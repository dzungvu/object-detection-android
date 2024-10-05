
<h2>Detector</h2>

build.gradle.kts (project) or settings.gradle.kts

```gradle.kts
maven {
            url = uri("https://maven.pkg.github.com/dzungvu/object-detection-android")
            credentials {
                username = localProperties["artifactory_username"] as String
                password = localProperties["artifactory_password"] as String
            }
        }
```
For username and password please [contact](skype:live:thedung2709?chat) for information


Init:</br></br>

```kotlin
private val detector by lazy { Detector(baseContext, this) }
```

<h2>DetectorListener:</h2>
Example</br></br>

```kotlin
override fun onEmptyDetect() {
    runOnUiThread {
        binding.overlay.clear()
        binding.overlay.invalidate()
    }
}

override fun onDetect(boundingBoxes: List<BoundingBox>, inferenceTime: Long) {
    runOnUiThread {
        binding.inferenceTime.text = "${inferenceTime}ms"
        binding.overlay.apply {
            setResults(boundingBoxes)
            invalidate()
        }
    }
}
```
