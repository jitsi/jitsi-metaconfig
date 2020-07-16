# Debugging

jitsi-metaconfig defines a simple `MetaconfigLogger` interface to enable logging.  jitsi-metaconfig logs debug messages when it searches through suppliers for a value which can be useful if it doesn't arrive at the value you suspect.

For example, to To set up the jitsi-metaconfig logger:
```kotlin
// For example, your app uses java.util.logger
val logger = Logger.getLogger("metaconfig")

val metaconfigLogger = object : MetaconfigLogger {
    override fun error(block: () -> String) { 
        logger.error(block())
    }  
    override fun warn(block: () -> String) { 
        logger.warn(block())
    }  
    override fun debug(block: () -> String) {
        logger.fine(block)
    }
}
```

### Logging when searching for a value
If you have a property:
```kotlin
val num: Int by config {  
    retrieve("some.missing.path".from(legacyConfig))  
    retrieve("new.num".from(newConfig))  
    retrieve("default value") { 8080 }  
}
```

The debug logs would show:
```
DEBUG: FallbackSupplier: checking for value via suppliers:
  ConfigSourceSupplier: key: 'some.missing.path', type: 'kotlin.Int', source: 'legacy config'
  ConfigSourceSupplier: key: 'new.num', type: 'kotlin.Int', source: 'new config'
  LambdaSupplier: 'default value'
DEBUG: ConfigSourceSupplier: Trying to retrieve key 'some.missing.path' from source 'legacy config' as type kotlin.Int
DEBUG: FallbackSupplier: failed to find value via ConfigSourceSupplier: key: 'some.missing.path', type: 'kotlin.Int', source: 'legacy config': org.jitsi.metaconfig.ConfigException$UnableToRetrieve$NotFound: not found
DEBUG: ConfigSourceSupplier: Trying to retrieve key 'new.num' from source 'new config' as type kotlin.Int
DEBUG: ConfigSourceSupplier: Successfully retrieved key 'new.num' from source 'new config' as type kotlin.Int
DEBUG: FallbackSupplier: value found via ConfigSourceSupplier: key: 'new.num', type: 'kotlin.Int', source: 'new config'

 ```
