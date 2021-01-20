package network;

import java.util.List;

import model.Task;
import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiInterface {

    @GET("inbox.json")
    Call<List<Task>> getInbox();

}