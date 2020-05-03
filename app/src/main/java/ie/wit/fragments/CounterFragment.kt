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
import ie.wit.utils.*
import kotlinx.android.synthetic.main.fragment_counter.*
import kotlinx.android.synthetic.main.fragment_counter.view.*
import kotlinx.android.synthetic.main.fragment_counter.view.progressBar
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import org.jetbrains.anko.toast
import java.lang.String.format
import java.util.HashMap


class CounterFragment : Fragment(), AnkoLogger {

    lateinit var app: WeightMatesApp
    var totalCounted = 0
    lateinit var loader : AlertDialog
    lateinit var eventListener : ValueEventListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = activity?.application as WeightMatesApp
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragment_counter, container, false)
        loader = createLoader(activity!!)
        activity?.title = getString(R.string.action_counter)

        root.progressBar.max = 10000
        root.caloriePicker.minValue = 1
        root.caloriePicker.maxValue = 3000

        root.caloriePicker.setOnValueChangedListener { picker, oldVal, newVal ->
            //Display the newly selected number to paymentAmount
            root.calorieAmount.setText("$newVal")
        }
        setButtonListener(root)
        return root;
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            CounterFragment().apply {
                arguments = Bundle().apply {}
            }
    }

    fun setButtonListener( layout: View) {
        layout.countButton.setOnClickListener {
            val amount = if (layout.calorieAmount.text.isNotEmpty())
                layout.calorieAmount.text.toString().toInt() else layout.caloriePicker.value
            if(totalCounted >= layout.progressBar.max)
                activity?.toast("Calorie Amount Exceeded!")
            else {
                val caloriemethod = if(layout.calorieMethod.checkedRadioButtonId == R.id.Calorie) "Calorie" else "KiloCalorie"
                writeNewCalorie(WeightMatesModel(calorietype = caloriemethod, amount = amount,
                    email = app.auth.currentUser?.email))
            }
        }
    }

    override fun onResume() {
        super.onResume()
        getTotalCounted(app.auth.currentUser?.uid)
    }

    override fun onPause() {
        super.onPause()
        app.database.child("user-calories")
            .child(app.auth.currentUser!!.uid)
            .removeEventListener(eventListener)
    }

    fun writeNewCalorie(weightMates: WeightMatesModel) {
        // Create new calorie at /calories & /calories/$uid
        showLoader(loader, "Adding Calories to Firebase")
        info("Firebase DB Reference : $app.database")
        val uid = app.auth.currentUser!!.uid
        val key = app.database.child("calories").push().key
        if (key == null) {
            info("Firebase Error : Key Empty")
            return
        }
        weightMates.uid = key
        val calorieValues = weightMates.toMap()

        val childUpdates = HashMap<String, Any>()
        childUpdates["/calories/$key"] = calorieValues
        childUpdates["/user-calories/$uid/$key"] = calorieValues

        app.database.updateChildren(childUpdates)
        hideLoader(loader)
    }

    fun getTotalCounted(userId: String?) {
        eventListener = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                info("Firebase Calorie error : ${error.message}")
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                totalCounted = 0
                val children = snapshot.children
                children.forEach {
                    val calorie = it.getValue<WeightMatesModel>(WeightMatesModel::class.java)
                    totalCounted += calorie!!.amount
                }
                progressBar.progress = totalCounted
                totalSoFar.text = format("$totalCounted")
            }
        }

        app.database.child("user-calories").child(userId!!)
            .addValueEventListener(eventListener)
    }
}