import android.content.Context
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley

class DatabaseManager(context: Context) {

    private val context: Context
    val queue: RequestQueue //file d'attente de requete

    init {
        this.context = context
        this.queue = Volley.newRequestQueue(context)
    }
}
