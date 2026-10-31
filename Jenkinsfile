// =============================================================================
//  News Portal — docker compose deployment pipeline
//
//  Builds and deploys the full stack (backend, frontend + infra services)
//  on the server itself. Jenkins must be on the same machine as the target.
//
// -----------------------------------------------------------------------------
//  A. Server / Jenkins prerequisites
// -----------------------------------------------------------------------------
//   1. Docker + Compose v2 installed; jenkins user in the docker group:
//        sudo usermod -aG docker jenkins && sudo systemctl restart jenkins
//   2. Jenkins plugins: Pipeline, Git, Credentials Binding
//   No Java, Node, or Gradle needed on the agent — all builds run inside Docker.
//
// -----------------------------------------------------------------------------
//  B. One credential to add in Jenkins
// -----------------------------------------------------------------------------
//   Manage Jenkins → Credentials → System → Global → Add credential
//     Kind:  Secret file
//     File:  your filled news-portal.env
//     ID:    NEWS_PORTAL_ENV        ← must match exactly
//
// -----------------------------------------------------------------------------
//  C. Create the Jenkins job
// -----------------------------------------------------------------------------
//   New Item → Pipeline → OK
//   Pipeline section:
//     Definition:   Pipeline script from SCM
//     SCM:          Git
//     URL:          https://github.com/MasumCse2k12/news-portal.git
//     Branch:       */main
//     Script Path:  Jenkinsfile
//   Triggers: Poll SCM or add a GitHub webhook.
//   Save → Build Now.
// =============================================================================

pipeline {
    agent any

    options {
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '15'))
        timeout(time: 45, unit: 'MINUTES')
        timestamps()
    }

    environment {
        COMPOSE_FILE = 'infrastructure/docker-compose.yml'
        APP_NAME     = 'news-portal'
    }

    stages {

        // ------------------------------------------------------------------
        stage('Checkout') {
        // ------------------------------------------------------------------
            steps {
                checkout scm
            }
        }

        // ------------------------------------------------------------------
        stage('Prepare .env') {
        // ------------------------------------------------------------------
            steps {
                withCredentials([file(credentialsId: 'NEWS_PORTAL_ENV', variable: 'ENV_FILE')]) {
                    sh 'cp "$ENV_FILE" .env'
                }
                sh '''
                    set -e
                    for var in DB_HOST DB_PORT DB_USER DB_PASSWORD JWT_SECRET \
                               MINIO_ACCESS_KEY MINIO_SECRET_KEY \
                               VITE_API_BASE_URL VITE_HOST VITE_PORT \
                               SERVER_HOST SERVER_PORT CORS_ORIGINS; do
                        grep -q "^${var}=" .env \
                            || { echo "ERROR: ${var} missing from .env"; exit 1; }
                        val=$(grep "^${var}=" .env | cut -d= -f2-)
                        [ -n "$val" ] \
                            || { echo "ERROR: ${var} is empty in .env"; exit 1; }
                    done

                    dupes=$(grep -oE "^[A-Za-z_][A-Za-z0-9_]*=" .env | sort | uniq -d || true)
                    [ -z "$dupes" ] \
                        || { echo "ERROR: duplicate keys in .env: $dupes"; exit 1; }

                    echo ".env validated."
                '''
            }
        }

        // ------------------------------------------------------------------
        stage('Build images') {
        // ------------------------------------------------------------------
            steps {
                sh '''
                    DOCKER_BUILDKIT=1 \
                    docker compose -f ${COMPOSE_FILE} -p ${APP_NAME} \
                        --env-file .env \
                        build --pull --no-cache backend

                    DOCKER_BUILDKIT=1 \
                    docker compose -f ${COMPOSE_FILE} -p ${APP_NAME} \
                        --env-file .env \
                        build --pull --no-cache frontend
                '''
            }
        }

        // ------------------------------------------------------------------
        stage('Deploy') {
        // ------------------------------------------------------------------
            steps {
                sh '''
                    set -e
                    COMPOSE="docker compose -f ${COMPOSE_FILE} -p ${APP_NAME} --env-file .env"
                    NETWORK="${APP_NAME}_default"

                    # Ensure the compose network exists before we try to wire
                    # external containers into it.
                    docker network create "$NETWORK" 2>/dev/null || true

                    # port_in_use <port>  — returns 0 if the port is already bound
                    port_in_use() {
                        local p=$1
                        ss     -tlnp 2>/dev/null | grep -q ":${p}[[:space:]]" && return 0
                        netstat -tlnp 2>/dev/null | grep -q ":${p}[[:space:]]" && return 0
                        nc -z 127.0.0.1 "${p}"   2>/dev/null                   && return 0
                        return 1
                    }

                    # wire_or_skip <service-name> <port>
                    # • Docker container holds the port → connect it into our network
                    #   under the service alias so the backend can reach it by name.
                    # • Native process holds the port → skip starting the service;
                    #   the .env must point to host-gateway for that service.
                    SKIP_INFRA=""
                    wire_or_skip() {
                        local svc=$1 port=$2
                        if port_in_use "${port}"; then
                            local cid
                            cid=$(docker ps --filter "publish=${port}" --format "{{.ID}}" | head -1)
                            if [ -n "$cid" ]; then
                                echo "Port ${port} held by Docker container ${cid} — reusing as '${svc}'"
                                docker network connect --alias "${svc}" "${NETWORK}" "${cid}" 2>/dev/null || true
                            else
                                echo "Port ${port} held by native process — skipping '${svc}' (ensure .env points to host-gateway)"
                            fi
                            SKIP_INFRA="${SKIP_INFRA} ${svc}"
                        fi
                    }

                    wire_or_skip postgres      5432
                    wire_or_skip redis         6379
                    wire_or_skip rabbitmq      5672
                    wire_or_skip minio         9000
                    wire_or_skip elasticsearch 9200

                    # Start only infra services that are NOT already handled above.
                    INFRA_START=""
                    for svc in postgres redis rabbitmq minio elasticsearch; do
                        echo "$SKIP_INFRA" | grep -qw "$svc" || INFRA_START="$INFRA_START $svc"
                    done

                    if [ -n "$INFRA_START" ]; then
                        echo "Starting infra services: $INFRA_START"
                        $COMPOSE up -d --no-build --no-deps $INFRA_START
                        echo "Waiting 15 s for infra to initialise..."
                        sleep 15
                    fi

                    # Start application services. --no-deps skips depends_on
                    # blocking so wired-in external services are not re-started.
                    echo "Starting app services: backend frontend"
                    $COMPOSE up -d --no-build --no-deps --remove-orphans backend frontend
                '''
            }
        }

        // ------------------------------------------------------------------
        stage('Container Status') {
        // ------------------------------------------------------------------
            steps {
                sh '''
                    echo "=== Running containers ==="
                    docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
                    echo ""
                    echo "=== Stack containers ==="
                    docker ps -a --filter "name=${APP_NAME}" \
                        --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
                '''
            }
        }

        // ------------------------------------------------------------------
        stage('Health Check') {
        // ------------------------------------------------------------------
            steps {
                script {
                    def apiPort = sh(
                        script: "grep '^SERVER_PORT=' .env | cut -d= -f2- || echo '8080'",
                        returnStdout: true
                    ).trim() ?: '8080'

                    def healthy = false
                    for (int i = 1; i <= 18; i++) {
                        sleep(time: 5, unit: 'SECONDS')
                        def rc = sh(
                            script: "curl -fs http://127.0.0.1:${apiPort}/actuator/health | grep -q '\"status\":\"UP\"'",
                            returnStatus: true
                        )
                        if (rc == 0) { healthy = true; break }
                        echo "Waiting for backend on port ${apiPort}… attempt ${i}/18"
                    }
                    if (!healthy) {
                        error('Backend did not become healthy within 90 s.')
                    }
                    echo "Backend healthy on port ${apiPort}."

                    def fePort = sh(
                        script: "grep '^VITE_PORT=' .env | cut -d= -f2- || echo '80'",
                        returnStdout: true
                    ).trim() ?: '80'

                    def rc = sh(
                        script: "curl -fso /dev/null http://127.0.0.1:${fePort}",
                        returnStatus: true
                    )
                    if (rc != 0) {
                        error("Frontend (Nginx) is not reachable on port ${fePort}.")
                    }
                    echo "Frontend healthy on port ${fePort}."
                }
            }
        }

        // ------------------------------------------------------------------
        stage('Prune old images') {
        // ------------------------------------------------------------------
            steps {
                sh 'docker image prune -f'
            }
        }
    }

    post {
        success {
            echo "Deployment successful — stack '${APP_NAME}' is live."
            sh 'rm -f .env'
        }
        failure {
            echo "Deployment FAILED. Dumping recent container logs…"
            sh '''
                ENV_OPT=""
                [ -f .env ] && ENV_OPT="--env-file .env"

                echo "=== backend ==="
                docker compose -f ${COMPOSE_FILE} -p ${APP_NAME} \
                    $ENV_OPT logs --tail=100 backend || true

                echo "=== frontend ==="
                docker compose -f ${COMPOSE_FILE} -p ${APP_NAME} \
                    $ENV_OPT logs --tail=30 frontend || true

                echo "=== postgres ==="
                docker compose -f ${COMPOSE_FILE} -p ${APP_NAME} \
                    $ENV_OPT logs --tail=20 postgres || true
            '''
            sh 'rm -f .env'
        }
        aborted {
            sh 'rm -f .env'
        }
    }
}
