
<h2>Detector</h2>
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