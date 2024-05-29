package fr.ro.recipemanager

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class User(
    val id: Int,
    val lastName: String,
    val firstName: String,
    val email: String,
    val role: String
)

class UserAdapter(private val userList: List<User>) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val lastNameTextView: TextView = itemView.findViewById(R.id.tvUserLastName)
        val firstNameTextView: TextView = itemView.findViewById(R.id.tvUserFirstName)
        val emailTextView: TextView = itemView.findViewById(R.id.tvUserEmail)
        val roleTextView: TextView = itemView.findViewById(R.id.tvUserRole)
        val editButton: ImageButton = itemView.findViewById(R.id.edit_button)
        val deleteButton: ImageButton = itemView.findViewById(R.id.delete_button)
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
            // Logic to edit the user
        }

        holder.deleteButton.setOnClickListener {
            // Logic to delete the user
        }
    }

    override fun getItemCount(): Int {
        return userList.size
    }
}
