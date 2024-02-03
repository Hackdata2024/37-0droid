import android.content.Context // Import the correct Android Context class
import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.devinfusion.hikisansih.R
import com.devinfusion.hikisansih.model.News
import java.util.Locale

class NewsAdapter(val context: Context, val mlist: ArrayList<News>) : RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {

    private lateinit var textToSpeech : TextToSpeech

    init {
        textToSpeech = TextToSpeech(context, TextToSpeech.OnInitListener { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = textToSpeech.setLanguage(Locale("en", "IN"))

                if (result == TextToSpeech.LANG_MISSING_DATA ||
                    result == TextToSpeech.LANG_NOT_SUPPORTED
                ) {
                    // Handle language not supported error
                    Toast.makeText(context, "Indian English not supported", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Handle TTS initialization failure
                Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show()
            }
        })

    }

    class NewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.newsTitle)
        val image: ImageView = itemView.findViewById(R.id.newsImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.news_layout, parent, false)
        return NewsViewHolder(view)
    }

    override fun getItemCount(): Int = mlist.size

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val current: News = mlist[position]
        holder.title.text = current.title
        Glide.with(context).load(current.image).into(holder.image)
        holder.itemView.setOnClickListener{
            val sentenceToSpeak = "Heading :  ${current.title} Description : ${current.description}"
            textToSpeech.speak(sentenceToSpeak, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }
}
