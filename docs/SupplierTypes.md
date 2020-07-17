# Supplier types

Different subclasses of `ConfigValueSupplier<T>` retrieve or manipulate values in different ways.

## ConfigSourceSupplier

Given a `String`, `ConfigSource` and a `KType`, queries the `ConfigSource` for a value at the path of the given `String` of type `KType`

The following example will try and retrieve the property at path "path.to.port" from `myConfigSource` as type `Int`:

```kotlin
val portSupplier: ConfigValueSupplier<Int> = ConfigSourceSupplier(
    "path.to.port",
    myConfigSource,
    typeOf<Int>()
)
```

## ValueTransformingSupplier
Given some `ConfigValueSupplier<T>` and a transformer function `(T) -> T`, `ValueTransformingSupplier` will transform the value retrieved from the original supplier into a different value.

The following example wraps a `ConfigSourceSupplier` which retrieves a `Boolean` with a `ValueTransformingSupplier` which transforms that retrieved value by inverting it:
```kotlin
val booleanSupplier: ConfigValueSupplier<Boolean> = ConfigSourceSupplier(
    "path.to.interval",
    myConfigSource,
    typeOf<Boolean>()
)
val inverted: ConfigValueSupplier<Boolean> =ValueTransformingSupplier(
    boolean,
    { !it }
)
```

## TypeConvertingSupplier
Given some `ConfigValueSupplier<OriginalType>` and a converter function `(OriginalType) -> NewType`, `TypeConvertingSupplier` will convert the value retrieved from the original supplier into a value with a different type.

The following example wraps a `ConfigSourceSupplier` which retrieves a `Long` with a `TypeConvertingSupplier` which converts that retrieved value to a `Duration`:
```kotlin
val longSupplier: ConfigValueSupplier<Long> = ConfigSourceSupplier(
    "path.to.interval",
    myConfigSource,
    typeOf<Long>()
)
val durationSupplier: ConfigValueSupplier<Duration> = TypeConvertingSupplier(
    longSupplier,
    { Duration.ofMillis(it) }
)
```

## FallbackSupplier
A `FallbackSupplier` aggregates one or more suppliers and tries each one, in order, until it finds a value.

The following example first checks key `legacy.path.port` in `legacyConfigSource` and, if the property isn't found there, checks key `new.path.port` in `newConfigSource`:
```kotlin
val port: ConfigValueSupplier<Int> = FallbackSupplier(
    listOf(
        ConfigSourceSupplier("legacy.path.port", legacyConfigSource, typeOf<Int>()),
        ConfigSourceSupplier("new.path.port", newConfigSource, typeOf<Int>())
    )
)
```

## LambdaSupplier
A `LambdaSupplier` is just a method which returns some value.  It can be useful when a configuration property originates in a place which is hard to map into a `ConfigSource`, like an object instance.

The following example is a `FallbackSupplier` which checks `legacyConfigSource` for `legacy.path.port` and, if it isn't found, checks the `LambdaSupplier` which pulls a value from another object.
```kotlin
val port: ConfigValueSupplier<Int> = FallbackSupplier(
    listOf(
        ConfigSourceSupplier("legacy.path.port", legacyConfigSource, typeOf<Int>()),
        LambdaSupplier { someLegacyConfigObject.port }
    )
)
```

## ConditionalSupplier
A `ConditionalSupplier` is a effectively a wrapper around a `FallbackSupplier` with a predicate guard.  Only if the predicate passes will it try to retrieve from the inner supplier.

This can be useful if a property should only be accessed based on some condition (like a feature being enabled).  The following example will throw [ConfigException.UnableToRetrieve.ConditionNotMet]
when calling `port.get()` if `serverEnabled` is not true:
```kotlin
val port: ConfigValueSupplier<Int> = ConditionalSupplier(
    { serverEnabled },
    listOf(
        ConfigSourceSupplier("legacy.path.port", legacyConfigSource, typeOf<Int>()),
        LambdaSupplier { someLegacyConfigObject.port }
    )
)
