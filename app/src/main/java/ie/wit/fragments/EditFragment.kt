package ie.wit.fragments


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

import ie.wit.R
import ie.wit.main.WeightMatesApp
import ie.wit.models.WeightMatesModel
import ie.wit.utils.createLoader
import ie.wit.utils.hideLoader
import ie.wit.utils.showLoader
import kotlinx.android.synthetic.main.fragment_edit.view.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info

class EditFragment : Fragment(), AnkoLogger {

    lateinit var app: WeightMatesApp
    lateinit var loader : AlertDialog
    lateinit var root: View
    var editCalorie: WeightMatesModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = activity?.application as WeightMatesApp

        arguments?.let {
            editCalorie = it.getParcelable("editcalorie")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_edit, container, false)
        activity?.title = getString(R.string.action_edit)
        loader = createLoader(activity!!)

        root.editAmount.setText(editCalorie!!.amount.toString())
        root.editcalorietype.setText(editCalorie!!.calorietype)

        root.editUpdateButton.setOnClickListener {
            showLoader(loader, "Updating Calorie on Server...")
            updateCalorieData()
            updateCalorie(editCalorie!!.uid, editCalorie!!)
            updateUserCalorie(app.auth.currentUser!!.uid,
                               editCalorie!!.uid, editCalorie!!)
        }

        return root
    }

    companion object {
        @JvmStatic
        fun newInstance(calorie: WeightMatesModel) =
            EditFragment().apply {
                arguments = Bundle().apply {
                    putParcelable("editcalorie",calorie)
                }
            }
    }

    fun updateCalorieData() {
        editCalorie!!.amount = root.editAmount.text.toString().toInt()
    }

    fun updateUserCalorie(userId: String, uid: String?, calorie: WeightMatesModel) {
        app.database.child("user-calories").child(userId).child(uid!!)
            .addListenerForSingleValueEvent(
                object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        snapshot.ref.setValue(calorie)
                        activity!!.supportFragmentManager.beginTransaction()
                        .replace(R.id.counterFrame, ReportFragment.newInstance())
                        .addToBackStack(null)
                        .commit()
                        hideLoader(loader)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        info("Firebase Calorie error : ${error.message}")
                    }
                })
    }

    fun updateCalorie(uid: String?, calorie: WeightMatesModel) {
        app.database.child("calories").child(uid!!)
            .addListenerForSingleValueEvent(
                object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        snapshot.ref.setValue(calorie)
                        hideLoader(loader)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        info("Firebase Calorie error : ${error.message}")
                    }
                })
    }
}
