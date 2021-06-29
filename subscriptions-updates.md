# Subscriptions Updates

## on Manual Update/Force Refresh (Update now)
When the user is using the _Update now_ feature, **all** _Subscriptions_ are downloaded, even if the user had a fresh version of it.

## on Periodic Updates (Automatic updates)
The `UpdateSubscriptionsWorker` is scheduled to run on "no less than 15 minutes" intervals. The system can delay the worker based on Connectivity criterias, battery status, etc.

When doing a periodic update, each active _Subscription_ is checked for expiration based on the current connection type:
On **unmetered** connections (Wifi) if the last successful download occurred more than 10 minutes ago or the file doesn't exists on the filesystem, it is considered **expired**
On **metered** connections (3g/4g/5g) if the last successful download occurred more than 25 minutes ago or the file doesn't exists on the filesystem, it is considered **expired**
If the _Subscription_ is expired a new version is downloaded (respecting `If-Modified-Since` and `If-None-
Match` headers)
If the _Subscription_ is not expired and the file still exists, we use the current file.

## on Configurations changes
When the user adds or removes a _Subscription_, adds/removes domains to the allow/block lists or changes the Acceptable Ads setting, a new `UpdateSubscriptionsWorker`is fired to run immediately.
Configuration changes are debounced by 500ms, so if the user quickly changes more than one setting, they will be combined in just one Worker, otherwise a new worker will be enqueued for each setting change.

### - Automatic update config changed
When the Automatic update configuration is changed, a new worker is scheduled do run every "at least" 15 minutes interval. (the system can delay the worker based on System constraints).
If the Automatic update setting is configured to **Wi-Fi Only**, the worker will run after the 6 hours interval and the device have an unmetered connection available. If the configuration is set to **Always**, the worker requires any working network connection.

### - No configuration changed since last update
If there is no changes on Active Subscriptions, allow/block lists and Acceptable Ads Status, and it is not a periodic or manual update, we skip the update altogether.

### - Adding/Removing domains from the allowlist
If the only change is on the allow/block lists, we simply check if the filters file for every active subscription are still present on the filesystem. If a file is missing we download the subscription again, if the file still exists it is used, no matter how long ago it was last fetched.

### - Adding/Removing Subscriptions, changing Acceptable Ads setting
When adding a new _Subscription_, if the filter list file already exists and the file was downloaded less than 5 minutes ago, that file is used instead of downloading it again. If the last successful download complete more than 1 hour ago, the _Subscription_ is downloaded again (respecting `If-Modified-Since` and `If-None-Match` headers).
Other active _Subscriptions_ are checked only for the existence of the filter list file. The same applies for Acceptable Ads, that are treat as any other _Subscription_ internally.

### Failures
If a subscription fails to download, a previously downloaded version will be used if available, otherwise the Filters list file will be created without this subscription.
When a worker fails to update a subscription it is marked on a `Retry` state and will be retried 4 times with an exponential backoff strategy.