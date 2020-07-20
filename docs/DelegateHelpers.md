# Delegate helpers
The [ConfigValueSupplier](SupplierTypes.md)s are the underlying code which does the work, but aren't very nice to use when defining properties.  Instead, there are helpers you can use to create property delegates for configuration property members.  [The examples in the README](../README.md#example-config-properties) show these in use.  We'll go over the helper methods here:

### Simple
To define a simple property which pulls only from a single source, use:
```kotlin
val myProperty: Int by config("path.to.property".from(myConfigSource))
```
---
### Fallback
To define a property which checks multiple configuration sources, stopping at the first value it finds, use:
```kotlin
val myProperty: Int by config {
    retrieve("legacy.path".from(legacyConfigSource))
    retrieve("new.path".from(newConfigSource))
}
```
This will first try to retrieve an `Int` at `legacy.path` from `legacyConfigSource`, if it isn't found, it will try to retrieve an `Int` at `new.path` from `newConfigSource`.

---
### Value/Type transformation
To transform the retrieved value in some way (here, by inverting the retrieved boolean), use:
```kotlin
val myProperty: Boolean by config {
    retrieve("path.to.property".from(myConfigSource).andTransformBy { !it })
}
```
This is useful if the semantics of a property were changed, for example:
> old_config.conf
> ```hocon
>app {
>   server {
>       enabled = false
>   }
>}
>```

> new_config.conf
> ```hocon
> app {
>     server {
>         disabled = false
>     }
> }
> ```
The property would be:
```kotlin
val serverEnabled: Boolean by config {
    retrieve("app.server.enabled".from(oldConfig))
    // Invert the value to match if it's 'enabled'
    retrieve("app.server.disabled".from(newConfig).andTransformBy { !it })
}
```
Converting the type of a value is also possible.  This is useful if you want the code to use a friendlier type than the config (say a `Duration` instead of a `Long` representing milliseconds):
```kotlin
val healthInterval: Duration by config {
    retrieve("app.health.interval".from(legacyConfigSource).asType<Long>().andConvertBy(Duration::ofMillis)
}
```
---
### Pulling a value from elsewhere
It's possible to pull a value from anywhere (e.g. a property of an object, or even just a hard-coded default) by passing a lambda:
```kotlin
val port: Int by config {
    retrieve("path.to.port".from(myConfig))
    // Since the lambda is opaque, the description gives some context
    retrieve("Foo::port") { foo.port }
}
```
This will first try to retrieve an `Int` at `path.to.port` from `myConfig` and, if it can't be found, will grab the `port` member of `foo`.  This could also be used to set a default value:

```kotlin
val port: Int by config {
    retrieve("path.to.port".from(myConfig))
    // Since the lambda is opaque, the description gives some context
    retrieve("default") { 8080 }
}
```
---
### Conditionally enabling a property
It's possible to guard access to a property if it should only be used based on the value of another property (e.g. a feature being enabled).

The following example will only allow accessing `port` if `serverEnabled` evaluates to true.  If it doesn't, then `ConfigException.UnableToRetrieve.ConditionNotMet` will be thrown.

```kotlin
val serverEnabled: Boolean by config("app.server.enabled".from(config))

val port: Int by config {
    onlyIf("Server is enabled", ::serverEnabled) {
        retrieve("path.to.port".from(myConfig))
        // Since the lambda is opaque, the description gives some context
        retrieve("default") { 8080 }
    }
}
```

---
### Optional properties
Properties which are optional (meaning they should have a value of `null` if not found anywhere), can be done like so:
```kotlin
val myProperty: Int? by optionalconfig("path.to.property".from(myConfigSource))
```
Or
```kotlin
val myProperty: Int? by optionalconfig {
    retrieve("path.to.property".from(myConfigSource))
    retrieve("new.path.to.property".from(myConfigSource))
}
```


### Deprecation of properties
Say you have the following json config:

> config.json
> ```json
>"app": {
>   "server": {
>     "enabled": "false"
>   }
>}
>```

And you want to move the property:
> config.json
> ```json
>"app": {
>   "api": {
>     "server": {
>       "enabled": "false"
>     }
>   }
> }
>```

You'd define a property in the code to look in both places, so deployments with the old configuration don't break:
```kotlin
val serverEnabled: Boolean by config {
    retrieve("app.server.enabled".from(myConfig))
    retrieve("app.api.server.enabled".from(myConfig))
}
```
But you want users to know that `app.server.enabled` is deprecated and they should use the new name/path.  You can mark the old path as deprecated:
```kotlin
val serverEnabled: Boolean by config {
    retrieve("app.server.enabled".from(myConfig).softDeprecated("use 'app.api.server.enabled'")
    retrieve("app.api.server.enabled".from(myConfig))
}
```
If a value is retrieved via "app.server.enabled" from myConfig, a warning will be logged:

> WARN: Key 'app.server.enabled' from source 'myConfig' is dprecated: use app.api.server.enabled

This warning is only printed once, and only if the value marked as deprecated was used as the "result".

Values can also be _hard_ deprecated:
```kotlin
val serverEnabled: Boolean by config {
    retrieve("app.server.enabled".from(myConfig).hardDeprecated("use 'app.api.server.enabled'")
    retrieve("app.api.server.enabled".from(myConfig))
}
```
And `ConfigException.UnableToRetrieve.Deprecated` will be thrown if that value is used as the result.

