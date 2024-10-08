import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.tripsetgo.R

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

       /* val itineraryCard: ImageView = view.findViewById(R.id.image_itinerary)
        val docsCard: ImageView = view.findViewById(R.id.image_docs)
        val photoCard: ImageView = view.findViewById(R.id.image_photo)
        val expenseCard: ImageView = view.findViewById(R.id.image_expense)

        itineraryCard.setOnClickListener {
            Toast.makeText(requireContext(), "Itinerary Clicked", Toast.LENGTH_SHORT).show()
            // Navigate to Itinerary page
        }

        docsCard.setOnClickListener {
            Toast.makeText(requireContext(), "Docs Clicked", Toast.LENGTH_SHORT).show()
            // Navigate to Docs page
        }

        photoCard.setOnClickListener {
            Toast.makeText(requireContext(), "Photo Clicked", Toast.LENGTH_SHORT).show()
            // Navigate to Photo page
        }

        expenseCard.setOnClickListener {
            Toast.makeText(requireContext(), "Expense Clicked", Toast.LENGTH_SHORT).show()
            // Navigate to Expense page
        }
*/
        return view
    }
}
