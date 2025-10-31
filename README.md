# ourclay
Reference Android Prototype to interact with BLE locks using Digital/Mobile Key (mkey_data)

## Links
- [ClaySDK docs](https://gitlab.com/claysolutions/public/clay-sdk/-/wikis/home)
- [Connect API spec](https://developer.saltosystems.com/ks/connect-api/reference/)

## ClaySDK / JustInSDK
To a regret, ClaySDK is not publicly distributed, so, avoiding the friction the best lib found in public access is ClaySDK 1.10.0.  
ClaySDK comes with JustInSDK, which handles BLE <-> Lock interactions and neither is publicly advertised.  
See [settings.gradle.kts](settings.gradle.kts) for the maven repo used.  
In case it's not anymore, the project includes [maven-releases.tar.gz](libs/maven-releases.tar.gz) as a backup:
- Extract it
- Uncomment [settings.gradle.kts](settings.gradle.kts):L21-22

## Prototype reference and MKey registration
Action takes place: [MainActivity.kt](app/src/main/java/ee/rofl/ourclay/MainActivity.kt)
  
- `API_PUBLIC_KEY` - public and static api key for the production Connect API
- `DEVICE_UID` - unique device identifier, come up w your own. corresponds to `GET /v1.2/me/devices (.items | .[] .device_uid)`  
Referred to as `installationUID` ClaySDK docs.
  
Device registration is not implemented for the prototype, so, manually:
- Take `claySdk.publicKey`, it's also reported as Log.d.
- Send `publicKey` to Connect API. `device.id` is just id, not `device_uid`  
Either create a new `device`, either use an existing one.
```curl
## [salto key] me device cert
curl -X "PUT" "https://connect.my-clay.com/v1.2/me/devices/:id/certificate" \
     -H 'Authorization: Bearer <your personal access_token>' \
     -H 'Content-Type: application/json' \
     -d $'{
  "public_key": "MFkw...IG0uoQ==\\n"
}'
```
- Get fresh `mkey_data` from `GET /v1.2/me/devices/:id/mkey`
- ...
- _Profit_

## Future plans
- or Remove Android dependency from ClaySDK/JustInSDK
- or Implement the BLE mkey exchange from scratch
- ...to make it usable outside of Android context