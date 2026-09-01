# Requires psql on PATH. Start the Java services once first so Hibernate can create tables.
#
# Hybrid / Compose DB ports (default):
#   user 5433, company 5434, job 5435, application 5436, preference 5439, resume 5440
# Native (one Postgres on 5432, six databases):
#   .\seed.ps1 -Native
#
# Usage:
#   $env:PGPASSWORD = 'your-db-password'
#   .\seed.ps1
#   .\seed.ps1 -Native -Username postgres -HostName localhost

param(
    [switch] $Native,
    [string] $HostName = "localhost",
    [string] $Username = "postgres"
)

$ErrorActionPreference = "Stop"
$here = Split-Path -Parent $MyInvocation.MyCommand.Path

function Invoke-Seed {
    param(
        [int] $Port,
        [string] $Database,
        [string] $File
    )
    Write-Host "Seeding $Database on port $Port ($File)"
    & psql -h $HostName -p $Port -U $Username -d $Database -v ON_ERROR_STOP=1 -f (Join-Path $here $File)
    if ($LASTEXITCODE -ne 0) {
        throw "psql failed for $Database (exit $LASTEXITCODE). Create tables by starting the matching Spring service first."
    }
}

if ($Native) {
    Invoke-Seed 5432 "job_portal_user" "01_job_portal_user.sql"
    Invoke-Seed 5432 "job_portal_company" "02_job_portal_company.sql"
    Invoke-Seed 5432 "job_portal_job" "03_job_portal_job.sql"
    Invoke-Seed 5432 "job_portal_resume" "04_job_portal_resume.sql"
    Invoke-Seed 5432 "job_portal_application" "05_job_portal_application.sql"
    Invoke-Seed 5432 "job_portal_preference" "06_job_portal_preference.sql"
} else {
    Invoke-Seed 5433 "job_portal_user" "01_job_portal_user.sql"
    Invoke-Seed 5434 "job_portal_company" "02_job_portal_company.sql"
    Invoke-Seed 5435 "job_portal_job" "03_job_portal_job.sql"
    Invoke-Seed 5440 "job_portal_resume" "04_job_portal_resume.sql"
    Invoke-Seed 5436 "job_portal_application" "05_job_portal_application.sql"
    Invoke-Seed 5439 "job_portal_preference" "06_job_portal_preference.sql"
}

Write-Host "Done. Login as admin@jobmate.local, employer@jobmate.local, or seeker@jobmate.local with password Demo@1234"
