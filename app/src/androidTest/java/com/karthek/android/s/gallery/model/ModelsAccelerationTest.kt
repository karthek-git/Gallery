package com.karthek.android.s.gallery.model

import android.content.res.AssetManager
import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Test
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.nnapi.NnApiDelegate
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.channels.FileChannel

class ModelsAccelerationTest {
	private fun loadModelFile(assetManager: AssetManager, path: String): ByteBuffer {
		val fd = assetManager.openFd(path)
		val fileChannel = FileInputStream(fd.fileDescriptor).channel
		return fileChannel.map(FileChannel.MapMode.READ_ONLY, fd.startOffset, fd.declaredLength)
	}

	@Test
	fun modelsAcceleration() {
		val context = InstrumentationRegistry.getInstrumentation().targetContext

		val options = Interpreter.Options()
		val nnApiDelegate = NnApiDelegate(NnApiDelegate.Options().setUseNnapiCpu(true).setAllowFp16(true))
		options.addDelegate(nnApiDelegate)

		var accelFailCount = 0
		try {
			val tfLite = Interpreter(loadModelFile(context.assets, "gic_uint8_v1.tflite"), options)
			tfLite.close()
		} catch (e: Exception) {
			accelFailCount++
			e.printStackTrace()
		}

		nnApiDelegate.close()
		Log.i("modelAccel", "Accel failed count: $accelFailCount")
		assertEquals(0, accelFailCount)
	}

}