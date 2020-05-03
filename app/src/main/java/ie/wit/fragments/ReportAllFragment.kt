package ie.wit.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

import ie.wit.R
import ie.wit.adapters.CalorieListener
import ie.wit.adapters.WeightMatesAdapter
import ie.wit.models.WeightMatesModel
import ie.wit.utils.*
import kotlinx.android.synthetic.main.fragment_report.view.*
import org.jetbrains.anko.info

class ReportAllFragment : ReportFragment(),
    CalorieListener {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_report, container, false)
        activity?.title = getString(R.string.menu_report_all)

        root.recyclerView.setLayoutManager(LinearLayoutManager(activity))
        setSwipeRefresh()

        return root
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            ReportAllFragment().apply {
                arguments = Bundle().apply { }
            }
    }

    override fun setSwipeRefresh() {
        root.swiperefresh.setOnRefreshListener(object : SwipeRefreshLayout.OnRefreshListener {
            override fun onRefresh() {
                root.swiperefresh.isRefreshing = true
                getAllUsersCalories()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        getAllUsersCalories()
    }

    fun getAllUsersCalories() {
        loader = createLoader(activity!!)
        showLoader(loader, "Downloading All Users Calories from Firebase")
        val caloriesList = ArrayList<WeightMatesModel>()
        app.database.child("calories")
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    info("Firebase Calorie error : ${error.message}")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    hideLoader(loader)
                    val children = snapshot.children
                    children.forEach {
                        val calorie = it.
                            getValue<WeightMatesModel>(WeightMatesModel::class.java)

                        caloriesList.add(calorie!!)
                        root.recyclerView.adapter =
                            WeightMatesAdapter(caloriesList, this@ReportAllFragment,true)
                        root.recyclerView.adapter?.notifyDataSetChanged()
                        checkSwipeRefresh()

                        app.database.child("calories").removeEventListener(this)
                    }
                }
            })
    }
}
