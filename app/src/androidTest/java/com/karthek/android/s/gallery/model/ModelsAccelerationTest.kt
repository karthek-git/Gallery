package com.karthek.android.s.gallery.model

import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import com.karthek.android.s.gallery.ml.EfficientnetLite2Uint82
import com.karthek.android.s.gallery.ml.FacenetMobileV1
import com.karthek.android.s.gallery.ml.ImageSceneUint81
import org.junit.Assert.assertEquals
import org.junit.Test
import org.tensorflow.lite.support.model.Model

class ModelsAccelerationTest {

	@Test
	fun modelsAcceleration() {
		val context = InstrumentationRegistry.getInstrumentation().targetContext
		val options = Model.Options.Builder().setDevice(Model.Device.NNAPI).build()
		var accelFailCount = 0
		try {
			EfficientnetLite2Uint82.newInstance(context, options)
		} catch (e: Exception) {
			accelFailCount++
			e.printStackTrace()
		}
		try {
			ImageSceneUint81.newInstance(context, options)
		} catch (e: Exception) {
			accelFailCount++
			e.printStackTrace()
		}
		try {
			FacenetMobileV1.newInstance(context, options)
		} catch (e: Exception) {
			accelFailCount++
			e.printStackTrace()
		}
		Log.i("modelAccel", "Accel failed count: $accelFailCount")
		assertEquals(0, accelFailCount)
	}

}