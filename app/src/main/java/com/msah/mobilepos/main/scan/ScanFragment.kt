package com.msah.mobilepos.main.scan

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.google.common.util.concurrent.ListenableFuture
import com.msah.mobilepos.R
import com.msah.mobilepos.databinding.FragmentScanBinding
import com.msah.mobilepos.loadingprogress.LoadingProgressBar
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import android.util.Size
import android.view.OrientationEventListener
import android.view.Surface
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.TorchState
import androidx.navigation.fragment.findNavController
import com.msah.mobilepos.data.model.Product
import com.msah.mobilepos.productdetail.ProductDetailsFragment
import com.msah.mobilepos.utils.Constants


class ScanFragment : Fragment() {


    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var bnd: FragmentScanBinding
    private lateinit var loadingProgressBar: LoadingProgressBar

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        bnd = DataBindingUtil.inflate(inflater, R.layout.fragment_scan, container, false)
        return bnd.root
    }

    private lateinit var cameraExecutor: ExecutorService
    private var flashEnabled = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


       askPermission(
            Manifest.permission.CAMERA,
            onGranted = {
                // Permission granted, handle location-related logic here
            },
            onDenied = { deniedPermissions ->

            }
        )



        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        // Initialize our background executor
        cameraExecutor = Executors.newSingleThreadExecutor()

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            bindPreview(cameraProvider)
        }, ContextCompat.getMainExecutor(requireContext()))

        bnd.overlay.post {
            bnd.overlay.setViewFinder()
        }


    }

    private fun bindPreview(cameraProvider: ProcessCameraProvider?) {
        cameraProvider?.unbindAll()

        val preview: Preview = Preview.Builder()
            .build()

        val cameraSelector: CameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        val imageAnalysis = ImageAnalysis.Builder()
            .setTargetResolution(Size(bnd.cameraPreview.width, bnd.cameraPreview.height))
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        val orientationEventListener = object : OrientationEventListener(requireContext()) {
            override fun onOrientationChanged(orientation : Int) {
                // Monitors orientation values to determine the target rotation value
                val rotation : Int = when (orientation) {
                    in 45..134 -> Surface.ROTATION_270
                    in 135..224 -> Surface.ROTATION_180
                    in 225..314 -> Surface.ROTATION_90
                    else -> Surface.ROTATION_0
                }

                imageAnalysis.targetRotation = rotation
            }
        }

        fun generateProductList(): List<Product> {
            return listOf(
                Product(
                    description = "A powerful and versatile laptop for everyday use.",
                    id = "5034624108328",
                    imgUrl = "https://fakestoreapi.com/img/71kWymZ+c+L._AC_SX679_.jpg",
                    price = 799.00,
                    name = "Laptop X10"
                ),
                Product(
                    description = "A captivating novel about love, loss, and redemption.",
                    id = "5034724308328",
                    imgUrl = "https://example.com/book.jpg",
                    price = 15.00,
                    name = "The Book of Lost Things"
                ),
                Product(
                    description = "A cozy throw blanket to keep you warm on chilly nights.",
                    id = "5034624308328",
                    imgUrl = "https://example.com/blanket.jpg",
                    price = 39.00,
                    name = "Soft Fleece Blanket"
                ),
                // Add more products as needed
            )
        }

        orientationEventListener.enable()

        class ScanningListener : ScanningResultListener {
            override fun onScanned(result: String) {
                requireActivity().runOnUiThread {
                    imageAnalysis.clearAnalyzer()
                    cameraProvider?.unbindAll()


                    for (product in generateProductList()) {
                        if (product.id == result) {
                            val prod = Product(
                                description = "A powerful and versatile laptop for everyday use.",
                                id = "5034624108328",
                                imgUrl = "https://fakestoreapi.com/img/71kWymZ+c+L._AC_SX679_.jpg",
                                price = 799.00,
                                name = "Laptop X10"
                            )


                            val bundle = Bundle().apply { putString(
                                Constants.PRODUCT_MODEL_NAME,
                                prod.toJson())
                            }


                            findNavController().navigate(R.id.action_searchFragment_to_productDetailsFragment, bundle)

//                            ProductDetailsFragment().run {
//                                arguments = bundle
//                                show(parentFragmentManager, "Details")
//                            }
                            break
                        }else{
                            Toast.makeText(context, "PRODUCT $result NOT FOUND", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

        var analyzer: ImageAnalysis.Analyzer = MLKitBarcodeAnalyzer(ScanningListener())


        imageAnalysis.setAnalyzer(cameraExecutor, analyzer)

        preview.setSurfaceProvider(bnd.cameraPreview.surfaceProvider)

        val camera =
            cameraProvider?.bindToLifecycle(this, cameraSelector, imageAnalysis, preview)

        if (camera?.cameraInfo?.hasFlashUnit() == true) {
            bnd.ivFlashControl.visibility = View.VISIBLE

            bnd.ivFlashControl.setOnClickListener {
                camera.cameraControl.enableTorch(!flashEnabled)
            }

            camera.cameraInfo.torchState.observe(viewLifecycleOwner) {
                it?.let { torchState ->
                    if (torchState == TorchState.ON) {
                        flashEnabled = true
                        bnd.ivFlashControl.setImageResource(R.drawable.ic_round_flash_on)
                    } else {
                        flashEnabled = false
                        bnd.ivFlashControl.setImageResource(R.drawable.ic_round_flash_off)
                    }
                }
            }
        }







    }
    fun askPermission(vararg permissions: String, onGranted: () -> Unit, onDenied: (List<String>) -> Unit) {
        val permissionResult = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissionsMap ->
            val deniedPermissions = permissionsMap.filterValues { !it }.keys.toList()
            if (deniedPermissions.isEmpty()) {
                onGranted()
            } else {
                onDenied(deniedPermissions)
            }
        }

        permissionResult.launch(permissions as Array<String>?)
    }

}
