#  This file is part of Adblock Plus <https://adblockplus.org/>,
#  Copyright (C) 2006-present eyeo GmbH
#
#  Adblock Plus is free software: you can redistribute it and/or modify
#  it under the terms of the GNU General Public License version 3 as
#  published by the Free Software Foundation.
#
#  Adblock Plus is distributed in the hope that it will be useful,
#  but WITHOUT ANY WARRANTY; without even the implied warranty of
#  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#  GNU General Public License for more details.
#
#  You should have received a copy of the GNU General Public License
#  along with Adblock Plus.  If not, see <http://www.gnu.org/licenses/>.

#!/usr/bin/env python3

"""
install pyjwt[crypto]
pip3 install pyjwt
export SERVICE_ACCOUNT_ID="your-service-account-id"
export private_key = "-----BEGIN RSA PRIVATE KEY-----
your private key
-----END RSA PRIVATE KEY-----"

Result of execution this script is JWT that expires after 10 minutes.

validation:

curl -X GET \
  -H  "Authorization: Bearer <your-access-token>" \
  -H  "service-account-id: your-service-account-id" \
  "https://devapi.samsungapps.com/auth/checkAccessToken"

Result successful result of the validation:
{"ok":true}

"""

import jwt
import time
import requests
import os

iat = round(time.time())
exp = iat + 600 # expire after ten minutes
service_account_id = os.environ['SERVICE_ACCOUNT_ID']
payload = {
    "iss": service_account_id,
    "scopes": ["publishing", "gss"],
    "iat": iat,
    "exp": exp
}

private_key = os.environ['PRIVATE_KEY']
signed_jwt = jwt.encode(payload=payload, key=private_key, algorithm="RS256")

# defining the API endpoint
API_ENDPOINT = "https://devapi.samsungapps.com/auth/accessToken"

# Authorization
authorization = "Bearer " + signed_jwt
# Header to be sent to API
headers = {
    'content-type': 'application/json',
    'Authorization': authorization
}

response = requests.post(url=API_ENDPOINT, headers=headers)

# get the access token from the response object
data_shows = response.json()
access_token = data_shows["createdItem"]["accessToken"]
print(access_token)
