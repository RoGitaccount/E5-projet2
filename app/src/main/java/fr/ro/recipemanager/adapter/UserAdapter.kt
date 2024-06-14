package fr.ro.recipemanager

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley

data class User(
    val id: Int,
    val lastName: String,
    val firstName: String,
    val email: String,
    val role: String
)

class UserAdapter(
    private val userList: MutableList<User>,
    private val onUserDeleted: () -> Unit // callback function
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val lastNameTextView: TextView = itemView.findViewById(R.id.tvUserLastName)
        val firstNameTextView: TextView = itemView.findViewById(R.id.tvUserFirstName)
        val emailTextView: TextView = itemView.findViewById(R.id.tvUserEmail)
        val roleTextView: TextView = itemView.findViewById(R.id.tvUserRole)
        val editButton: ImageView = itemView.findViewById(R.id.edit_button)
        val deleteButton: ImageView = itemView.findViewById(R.id.delete_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_users, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]
        holder.lastNameTextView.text = user.lastName
        holder.firstNameTextView.text = user.firstName
        holder.emailTextView.text = user.email
        holder.roleTextView.text = user.role

        holder.editButton.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, UpdateUserActivity::class.java).apply {
                putExtra("id_utilisateur", user.id)
                putExtra("user_last_name", user.lastName)
                putExtra("user_first_name", user.firstName)
                putExtra("user_email", user.email)
                putExtra("user_role", user.role)
            }
            context.startActivity(intent)
        }

        holder.deleteButton.setOnClickListener {
            deleteUser(holder.itemView.context, user.id) { success ->
                if (success) {
                    userList.removeAt(position)
                    notifyItemRemoved(position)
                    notifyItemRangeChanged(position, userList.size)
                    Toast.makeText(holder.itemView.context, "Utilisateur supprimé avec succès", Toast.LENGTH_SHORT).show()
                    onUserDeleted()
                } else {
                    Toast.makeText(holder.itemView.context, "Erreur lors de la suppression de l'utilisateur", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    private fun deleteUser(context: Context, userId: Int, callback: (Boolean) -> Unit) {
        val queue = Volley.newRequestQueue(context)
        val url = "http://192.168.56.1/api/actions/deleteUser.php?id_utilisateur=$userId"

        val stringRequest = StringRequest(
            Request.Method.GET, url,
            Response.Listener<String> { response ->
                // Handle the response
                callback(true)
            },
            Response.ErrorListener {
                // Handle the error
                callback(false)
            })

        queue.add(stringRequest)
    }
}
