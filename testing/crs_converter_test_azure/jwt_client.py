import os
import msal
import logging
import time
logging.basicConfig(
    level=os.environ.get("LOG_LEVEL", "INFO"),
    format="%(asctime)s %(levelname)s %(name)s %(message)s",
    datefmt="%Y-%m-%d %H:%M:%S"
)
logging.Formatter.converter = time.gmtime
logger = logging.getLogger(__name__)

def get_id_token():
    # Generate valid Token for given tenant.
    tenant_id = os.getenv('AZURE_TENANT_ID')
    resource_id = os.getenv('AZURE_AD_APP_RESOURCE_ID')
    client_id = os.getenv('INTEGRATION_TESTER')
    client_secret = os.getenv('AZURE_TESTER_SERVICEPRINCIPAL_SECRET')
    authority_host_uri = 'https://login.microsoftonline.com'
    authority_uri = authority_host_uri + '/' + tenant_id
    scopes = [resource_id + '/.default']
    integration_tester_access_token = os.getenv('INTEGRATION_TESTER_ACCESS_TOKEN')

    if integration_tester_access_token:
        logger.info("Using bearer token from environment variable: INTEGRATION_TESTER_ACCESS_TOKEN")
        return integration_tester_access_token
    else:
        logger.info("Generating bearer token using SPN client id and secret since environment variable INTEGRATION_TESTER_ACCESS_TOKEN is null/not defined")

        # Log warnings if any required variable is missing
        if not tenant_id:
            logger.warning("AZURE_TENANT_ID is not set or empty.")
        if not resource_id:
            logger.warning("AZURE_AD_APP_RESOURCE_ID is not set or empty.")
        if not client_id:
            logger.warning("INTEGRATION_TESTER is not set or empty.")
        if not client_secret:
            logger.warning("AZURE_TESTER_SERVICEPRINCIPAL_SECRET is not set or empty.")

    try:
        app = msal.ConfidentialClientApplication(client_id=client_id, authority=authority_uri, client_credential=client_secret)
        result = app.acquire_token_for_client(scopes=scopes)
        return result.get('access_token')
    except Exception as e:
        print(e)

def get_invalid_token():
    '''
    This is dummy jwt
    {
         "sub": "dummy@dummy.com",
         "iss": "dummy@dummy.com",
         "aud": "dummy.dummy.com",
         "iat": 1556137273,
         "exp": 1556223673,
         "provider": "dummy.com",
         "client": "dummy.com",
         "userid": "dummytester.com",
         "email": "dummytester.com",
         "authz": "",
         "lastname": "dummy",
         "firstname": "dummy",
         "country": "",
         "company": "",
         "jobtitle": "",
         "subid": "dummyid",
         "idp": "dummy",
         "hd": "dummy.com",
         "desid": "dummyid",
         "contact_email": "dummy@dummy.com"
    }
    '''
    return "fail.azure"
