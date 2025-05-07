import com.pcloudy.bankingg.ATMDetails
import com.pcloudy.bankingg.PostalDetails
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.json.JSONArray
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ATMFinderService {
//    companion object {
//        private const val HERE_API_KEY = "ZmsUcy9qkCXmvatFLGX-L9kj2XyfH-GCZbUOBZNqj7U"
//        private const val POSTAL_API_BASE_URL = "https://api.postalpincode.in/pincode/"
//    }
//
//    private val httpClient = OkHttpClient.Builder()
//        .connectTimeout(10, TimeUnit.SECONDS)
//        .readTimeout(10, TimeUnit.SECONDS)
//        .build()
//
//    suspend fun findNearbyATMs(pincode: String): List<ATMDetails> = withContext(Dispatchers.IO) {
//        try {
//            val postalDetails = getPostalDetails(pincode)
//            if (postalDetails != null) {
//                findATMsUsingHereAPI(postalDetails)
//            } else {
//                emptyList()
//            }
//        } catch (e: Exception) {
//            println("Error finding ATMs: ${e.message}")
//            emptyList()
//        }
//    }
//
//    private suspend fun getPostalDetails(pincode: String): PostalDetails? = withContext(Dispatchers.IO) {
//        try {
//            val request = Request.Builder()
//                .url(POSTAL_API_BASE_URL + pincode)
//                .build()
//
//            val response = httpClient.newCall(request).execute()
//            val responseBody = response.body?.string()
//
//            if (responseBody != null) {
//                val jsonArray = JSONArray(responseBody)
//                val data = jsonArray.getJSONObject(0)
//
//                if ("Success" == data.getString("Status")) {
//                    val postOffice = data.getJSONArray("PostOffice").getJSONObject(0)
//                    PostalDetails(
//                        city = postOffice.getString("Block"),
//                        district = postOffice.getString("District"),
//                        state = postOffice.getString("State"),
//                        latitude = getLatitudeFromCity(postOffice.getString("Block")),
//                        longitude = getLongitudeFromCity(postOffice.getString("Block"))
//                    )
//                } else null
//            } else null
//        } catch (e: Exception) {
//            println("Error getting postal details: ${e.message}")
//            null
//        }
//    }
//
//    private suspend fun findATMsUsingHereAPI(postalDetails: PostalDetails): List<ATMDetails> = withContext(Dispatchers.IO) {
//        try {
//            val radius = 5000 // 5km radius
//            val url = "https://discover.search.hereapi.com/v1/discover" +
//                    "?at=${postalDetails.latitude},${postalDetails.longitude}" +
//                    "&q=ATM" +
//                    "&limit=20" +
//                    "&radius=$radius" +
//                    "&apiKey=$HERE_API_KEY"
//
//            val request = Request.Builder()
//                .url(url)
//                .build()
//
//            val response = httpClient.newCall(request).execute()
//            val responseBody = response.body?.string()
//
//            if (responseBody != null) {
//                val jsonResponse = JSONObject(responseBody)
//                val items = jsonResponse.getJSONArray("items")
//
//                val atmList = mutableListOf<ATMDetails>()
//                for (i in 0 until items.length()) {
//                    val item = items.getJSONObject(i)
//                    val address = item.getJSONObject("address")
//                    val position = item.getJSONObject("position")
//
//                    atmList.add(
//                        ATMDetails(
//                            name = item.optString("title", "ATM"),
//                            operator = item.optString("chainName", "Bank ATM"),
//                            address = buildAddress(address),
//                            latitude = position.getDouble("lat"),
//                            longitude = position.getDouble("lng"),
//                            distance = item.optDouble("distance", 0.0),
//                            isOpen24Hours = item.optJSONObject("extended")
//                                ?.optString("openingHours", "")
//                                ?.contains("24/7") ?: false
//                        )
//                    )
//                }
//                atmList
//            } else emptyList()
//        } catch (e: Exception) {
//            println("Error finding ATMs with HERE API: ${e.message}")
//            emptyList()
//        }
//    }
//
//    private fun buildAddress(addressObj: JSONObject): String {
//        return buildString {
//            addressObj.optString("houseNumber", "").let { if (it.isNotEmpty()) append("$it, ") }
//            addressObj.optString("street", "").let { if (it.isNotEmpty()) append("$it, ") }
//            addressObj.optString("district", "").let { if (it.isNotEmpty()) append("$it, ") }
//            addressObj.optString("city", "").let { if (it.isNotEmpty()) append("$it, ") }
//            addressObj.optString("postalCode", "").let { if (it.isNotEmpty()) append("$it, ") }
//            addressObj.optString("state", "").let { if (it.isNotEmpty()) append(it) }
//        }.trimEnd(',', ' ')
//    }
//
//    private fun getLatitudeFromCity(city: String): Double = 19.0760  // Default to Mumbai
//    private fun getLongitudeFromCity(city: String): Double = 72.8777 // Default to Mumbai
}