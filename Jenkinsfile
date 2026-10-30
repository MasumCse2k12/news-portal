pipeline {
    agent any

    environment {
        DOCKER_REGISTRY = 'your-registry.io/newsportal'
        APP_NAME        = 'news-portal-api'
        FRONTEND_NAME   = 'news-portal-web'
        SONAR_PROJECT   = 'news-portal'

        // Container names for test dependencies — unique per build to allow parallel runs
        ES_CONTAINER      = "np-es-${BUILD_NUMBER}"
        RABBIT_CONTAINER  = "np-rabbit-${BUILD_NUMBER}"
        ES_PORT           = "9200"
        RABBIT_PORT       = "5672"
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timeout(time: 30, unit: 'MINUTES')
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                sh 'git log --oneline -5'
            }
        }

        stage('Start Test Services') {
            steps {
                sh """
                    docker run -d --name ${ES_CONTAINER} \
                        -p ${ES_PORT}:9200 \
                        -e "discovery.type=single-node" \
                        -e "xpack.security.enabled=false" \
                        -e "ES_JAVA_OPTS=-Xms256m -Xmx256m" \
                        elasticsearch:8.11.4

                    docker run -d --name ${RABBIT_CONTAINER} \
                        -p ${RABBIT_PORT}:5672 \
                        rabbitmq:3.13-alpine

                    echo "Waiting for Elasticsearch..."
                    timeout 60 sh -c 'until curl -sf http://localhost:${ES_PORT}/_cluster/health; do sleep 3; done'

                    echo "Waiting for RabbitMQ..."
                    timeout 60 sh -c 'until nc -z localhost ${RABBIT_PORT}; do sleep 3; done'

                    echo "Test services ready."
                """
            }
        }

        stage('Backend: Test') {
            steps {
                dir('backend') {
                    sh './gradlew test -Dspring.profiles.active=test --no-daemon --no-build-cache'
                    junit 'build/test-results/test/**/*.xml'
                }
            }
            post {
                always {
                    jacoco(execPattern: 'backend/build/jacoco/*.exec')
                }
            }
        }

        stage('Stop Test Services') {
            steps {
                sh """
                    docker stop ${ES_CONTAINER} ${RABBIT_CONTAINER} || true
                    docker rm   ${ES_CONTAINER} ${RABBIT_CONTAINER} || true
                """
            }
        }

        stage('Backend: Code Quality') {
            when { branch pattern: 'develop|main', comparator: 'REGEXP' }
            steps {
                dir('backend') {
                    withSonarQubeEnv('SonarQube') {
                        sh './gradlew sonar -Dsonar.projectKey=${SONAR_PROJECT} --no-daemon'
                    }
                }
            }
        }

        stage('Backend: Security Scan') {
            when { branch 'main' }
            steps {
                dir('backend') {
                    sh './gradlew dependencyCheckAnalyze --no-daemon'
                }
            }
            post {
                always {
                    dependencyCheckPublisher pattern: 'backend/build/reports/dependency-check-report.xml'
                }
            }
        }

        stage('Backend: Build JAR') {
            steps {
                dir('backend') {
                    sh './gradlew bootJar -x test --no-daemon'
                    archiveArtifacts artifacts: 'build/libs/news-portal-api.jar'
                }
            }
        }

        stage('Frontend: Build') {
            steps {
                dir('frontend') {
                    sh 'npm ci'
                    sh 'npm run build'
                }
            }
        }

        stage('Docker: Build & Push') {
            when { branch pattern: 'develop|main', comparator: 'REGEXP' }
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: 'docker-registry-creds',
                        usernameVariable: 'DOCKER_USER',
                        passwordVariable: 'DOCKER_PASS'
                    ),
                    file(
                        credentialsId: 'NEWS_PORTAL_ENV',
                        variable: 'ENV_FILE'
                    )
                ]) {
                    sh "echo $DOCKER_PASS | docker login ${DOCKER_REGISTRY} -u $DOCKER_USER --password-stdin"

                    sh """
                        export \$(grep -v '^#' \$ENV_FILE | xargs)
                        docker build \
                            --build-arg VITE_API_BASE_URL=\$VITE_API_BASE_URL \
                            -t ${DOCKER_REGISTRY}/${FRONTEND_NAME}:${BUILD_NUMBER} \
                            ./frontend
                        docker build \
                            -t ${DOCKER_REGISTRY}/${APP_NAME}:${BUILD_NUMBER} \
                            ./backend
                        docker push ${DOCKER_REGISTRY}/${FRONTEND_NAME}:${BUILD_NUMBER}
                        docker push ${DOCKER_REGISTRY}/${APP_NAME}:${BUILD_NUMBER}
                    """
                }
            }
        }

        stage('DB Migration: Staging') {
            when { branch 'develop' }
            steps {
                withCredentials([file(credentialsId: 'NEWS_PORTAL_ENV', variable: 'ENV_FILE')]) {
                    dir('backend') {
                        sh """
                            export \$(grep -v '^#' \$ENV_FILE | xargs)
                            ./gradlew flywayMigrate \
                                -Dflyway.url=\$DB_URL \
                                -Dflyway.user=\$DB_USER \
                                -Dflyway.password=\$DB_PASSWORD \
                                --no-daemon
                        """
                    }
                }
            }
        }

        stage('Deploy: Staging') {
            when { branch 'develop' }
            steps {
                withCredentials([file(credentialsId: 'NEWS_PORTAL_ENV', variable: 'ENV_FILE')]) {
                    sh """
                        docker-compose -f infrastructure/docker-compose.yml \
                            --env-file \$ENV_FILE \
                            up -d --no-deps backend frontend
                    """
                }
                echo "Staging deployed: ${DOCKER_REGISTRY}/${APP_NAME}:${BUILD_NUMBER}"
            }
        }

        stage('Deploy: Production') {
            when { branch 'main' }
            input {
                message "Deploy ${APP_NAME}:${BUILD_NUMBER} to PRODUCTION?"
                ok "Deploy Now"
                submitter "admin,lead-engineer"
            }
            steps {
                withCredentials([file(credentialsId: 'NEWS_PORTAL_ENV', variable: 'ENV_FILE')]) {
                    dir('backend') {
                        sh """
                            export \$(grep -v '^#' \$ENV_FILE | xargs)
                            ./gradlew flywayMigrate \
                                -Dflyway.url=\$DB_URL \
                                -Dflyway.user=\$DB_USER \
                                -Dflyway.password=\$DB_PASSWORD \
                                --no-daemon
                        """
                    }
                    echo "Deploying ${DOCKER_REGISTRY}/${APP_NAME}:${BUILD_NUMBER} to production..."
                    sh """
                        docker-compose -f infrastructure/docker-compose.yml \
                            --env-file \$ENV_FILE \
                            up -d --no-deps backend frontend
                    """
                }
            }
        }
    }

    post {
        always {
            // Safety net: clean up test containers if any stage failed before Stop Test Services
            sh """
                docker stop ${ES_CONTAINER} ${RABBIT_CONTAINER} 2>/dev/null || true
                docker rm   ${ES_CONTAINER} ${RABBIT_CONTAINER} 2>/dev/null || true
            """
            cleanWs()
        }
        success {
            echo "Pipeline succeeded: ${APP_NAME} build ${BUILD_NUMBER}"
        }
        failure {
            emailext(
                subject: "FAILED: ${APP_NAME} build ${BUILD_NUMBER}",
                body: "Build ${BUILD_NUMBER} failed. Check: ${BUILD_URL}",
                to: 'team@newsportal.com'
            )
        }
    }
}
