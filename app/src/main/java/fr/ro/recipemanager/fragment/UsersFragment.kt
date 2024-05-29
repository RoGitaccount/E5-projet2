package fr.ro.recipemanager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException

class UsersFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var userAdapter: UserAdapter
    private var userList: MutableList<User> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_user, container, false)
        recyclerView = view.findViewById(R.id.User_recycler_list)
        recyclerView.layoutManager = LinearLayoutManager(context)
        userAdapter = UserAdapter(userList)
        recyclerView.adapter = userAdapter
        loadUsers()
        return view
    }

    private fun loadUsers() {
        val queue = Volley.newRequestQueue(context)
        val url = "http://192.168.56.1/api/actions/showUser.php"





        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, url, null,
            { response ->
                try {
                    if (response.getBoolean("success")) {
                        val users = response.getJSONArray("utilisateurs")
                        for (i in 0 until users.length()) {
                            val user = users.getJSONObject(i)
                            userList.add(
                                User(
                                    user.getInt("id_utilisateur"),
                                    user.getString("nom"),
                                    user.getString("prenom"),
                                    user.getString("email"),
                                    user.getString("Role")
                                )
                            )
                        }
                        userAdapter.notifyDataSetChanged()
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            { error ->
                error.printStackTrace()
            }
        )
        queue.add(jsonObjectRequest)
    }
}
