package view

import Adapter.TarifAdapter
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.example.yemektarifuygulama.databinding.FragmentListeBinding
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import model.Tarif
import roomdb.TarifDAO
import roomdb.TarifDatabase

class ListeFragment : Fragment() {
    private var _binding: FragmentListeBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: TarifDatabase
    private lateinit var tarifDao: TarifDAO
    private val mDisposable= CompositeDisposable()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db= Room.databaseBuilder(requireContext(),TarifDatabase::class.java,"arifler").build()
        tarifDao=db.TarifDAO()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentListeBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) { //Tüm ilgili arayüz kodları buraya yazılır.
        super.onViewCreated(view, savedInstanceState)
        binding.floatingActionButton2.setOnClickListener {
            val action=ListeFragmentDirections.actionListeFragmentToTarifFragment("yeni",0)
            Navigation.findNavController(view).navigate(action)
        }
        binding.tarifRecyclerView.layoutManager=LinearLayoutManager(requireContext())
        verileriAl()
    }
    private fun verileriAl(){
        mDisposable.add(
            tarifDao.getAll()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleResponse)
        )
    }
    private fun handleResponse(tarifler:List<Tarif>){
        val adapter=TarifAdapter(tarifler)
        binding.tarifRecyclerView.adapter=adapter
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        mDisposable.clear()
    }
}