package view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import androidx.room.Room
import com.example.yemektarifuygulama.databinding.FragmentTarifBinding
import com.google.android.material.snackbar.Snackbar
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import model.Tarif
import roomdb.TarifDAO
import roomdb.TarifDatabase
import java.io.ByteArrayOutputStream

class TarifFragment : Fragment() {


    private var _binding: FragmentTarifBinding? = null
    private val binding get() = _binding!!
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private var secilenGorsel: Uri?=null
    private var secilenBitmap: Bitmap?=null
    private lateinit var db:TarifDatabase
    private lateinit var tarifDao:TarifDAO
    private val mDisposable= CompositeDisposable()
    private var secilenTarif:Tarif?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerLauncher()
        db= Room.databaseBuilder(requireContext(),TarifDatabase::class.java,"arifler").build()
        tarifDao=db.TarifDAO()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTarifBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.imageView.setOnClickListener{ gorselSec(it) }
        binding.kaydetButon.setOnClickListener { kaydet(it) }
        binding.silButon.setOnClickListener { sil(it) }
        binding.guncelle.setOnClickListener { guncelle((it)) }

        arguments?.let {
            val bilgi = TarifFragmentArgs.fromBundle(it).bilgi
            if (bilgi == "yeni") {
                binding.silButon.isEnabled = false
                binding.kaydetButon.isEnabled = true
                binding.editTextText.setText("")
                binding.editTextText2.setText("")
            } else {
                binding.silButon.isEnabled = true
                binding.kaydetButon.isEnabled = false
                val id=TarifFragmentArgs.fromBundle(it).id
                mDisposable.add(
                    tarifDao.findById(id)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this::handleResponse)

                )
            }
        }
    }
    private fun handleResponse(tarif: Tarif){
        val bitmap=BitmapFactory.decodeByteArray(tarif.gorsel,0,tarif.gorsel.size)
        binding.imageView.setImageBitmap(bitmap)
        binding.editTextText.setText(tarif.isim)
        binding.editTextText2.setText(tarif.malzeme)
        secilenTarif=tarif

    }

    fun kaydet(view: View) {
        val isim=binding.editTextText.text.toString()
        val malzeme=binding.editTextText2.text.toString()
        if(secilenBitmap!=null){
            val kucukBitmap=kucukBitmapOlustur(secilenBitmap!!,300)
            val outputStream=ByteArrayOutputStream()
            kucukBitmap.compress(Bitmap.CompressFormat.PNG,50,outputStream)
            val byteDizisi=outputStream.toByteArray()

            val tarif= Tarif(isim,malzeme,byteDizisi)

            //Rx Java
            mDisposable.add(
                tarifDao.insert(tarif)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleResponseInsert))
        }

    }

    private fun handleResponseInsert(){
        //Bir önceki fragmente dönme
        val action=TarifFragmentDirections.actionTarifFragmentToListeFragment()
        Navigation.findNavController(requireView()).navigate(action)

    }

    fun sil(view: View) {
        if(secilenTarif!=null){
            mDisposable.add(
                tarifDao.delete(tarif=secilenTarif!!)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::handleResponseInsert)
            )
        }
    }

    fun guncelle(view: View,) {
        if(secilenTarif!=null){
            //Rx Java
            mDisposable.add(
                        tarifDao.update(tarif = secilenTarif!!)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe())
        }
    }

    fun gorselSec(view: View) {
        activity?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(
                        requireActivity().applicationContext,
                        Manifest.permission.READ_MEDIA_IMAGES
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(
                            requireActivity(),
                            Manifest.permission.READ_MEDIA_IMAGES
                        )
                    ) {
                        Snackbar.make(
                            view,
                            "Permission needed for gallery",
                            Snackbar.LENGTH_INDEFINITE
                        ).setAction("Give Permission",
                            View.OnClickListener {
                                permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                            }).show()
                    } else {
                        permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                    }
                } else {
                    val intentToGallery =
                        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    activityResultLauncher.launch(intentToGallery)

                }
            } else {
                if (ContextCompat.checkSelfPermission(
                        requireActivity().applicationContext,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(
                            requireActivity(),
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        )
                    ) {
                        Snackbar.make(
                            view,
                            "Permission needed for gallery",
                            Snackbar.LENGTH_INDEFINITE
                        ).setAction("Give Permission",
                            View.OnClickListener {
                                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                            }).show()
                    } else {
                        permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                    }
                } else {
                    val intentToGallery =
                        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    activityResultLauncher.launch(intentToGallery)

                }
            }
        }
    }


    private fun registerLauncher() {
        activityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if(result.resultCode==AppCompatActivity.RESULT_OK){
                    val intentFromResult=result.data
                    if(intentFromResult!=null){
                       secilenGorsel=intentFromResult.data
                        try{
                        if(Build.VERSION.SDK_INT>=28){
                            val source=ImageDecoder.createSource(requireActivity().contentResolver,secilenGorsel!!)
                            secilenBitmap=ImageDecoder.decodeBitmap(source)
                            binding.imageView.setImageBitmap(secilenBitmap)
                        }
                        else {
                            secilenBitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver,secilenGorsel)
                            binding.imageView.setImageBitmap(secilenBitmap)
                        }
                    } catch (e:Exception) {
                            println(e.localizedMessage)
                        }
                    }
                }
            }

        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
                if (result) {
                    //izin verildi,galeriye gidilir
                    val intentToGalery=Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    activityResultLauncher.launch(intentToGalery)
                } else {
                    Toast.makeText(requireContext(), "izin verilmedi!", Toast.LENGTH_LONG)
                        .show()
                }
            }
    }
    private fun kucukBitmapOlustur(kullanicininSectigiBitmap: Bitmap,maximumBoyut:Int):Bitmap{
        var width=kullanicininSectigiBitmap.width
        var height=kullanicininSectigiBitmap.height
        val bitmapOrani :Double=width.toDouble()/height.toDouble()

        if(bitmapOrani>1){
            //görsel yatay
            width=maximumBoyut
            val kisaltilmisYukseklik=width/bitmapOrani
            height=kisaltilmisYukseklik.toInt()
        }else{
            //görsel dikey
            height=maximumBoyut
            val kisaltilmisGenislik=height*bitmapOrani
            width=kisaltilmisGenislik.toInt()
        }
        return Bitmap.createScaledBitmap(kullanicininSectigiBitmap,100,100,true)
    }

        override fun onDestroyView() {
            super.onDestroyView()
            _binding = null
            mDisposable.clear()
        }
}