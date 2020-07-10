package com.codepath.teleroid.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codepath.teleroid.MainActivity;
import com.codepath.teleroid.R;
import com.codepath.teleroid.SomeonesProfileActivity;
import com.codepath.teleroid.databinding.ActivityMainBinding;
import com.codepath.teleroid.databinding.ItemPostBinding;
import com.codepath.teleroid.fragments.ProfileFragment;
import com.codepath.teleroid.models.Like;
import com.codepath.teleroid.models.Post;
import com.codepath.teleroid.utilities.DateUtility;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;

import org.parceler.Parcels;

import java.util.List;

public class DetailedPostsAdapter extends RecyclerView.Adapter<DetailedPostsAdapter.ViewHolder> {

    public static final String TAG = DetailedPostsAdapter.class.getSimpleName(); //logging purposes

    private final FragmentManager fragmentManager;

    private Context context;
    private List<Post> posts;

    public DetailedPostsAdapter(Context context, List<Post> posts){
        this.context = context;
        this.posts = posts;
        this.fragmentManager = ((AppCompatActivity) context).getSupportFragmentManager();

    }

    // Clean all elements of the recycler
    public void clear() {
        posts.clear();
        notifyDataSetChanged();
    }

    // Add a list of items -- change to type used
    public void addAll(List<Post> newPosts) {
        posts.addAll(newPosts);
        notifyDataSetChanged();
        int i = 1;
        for(Post post: posts){
            Log.d(TAG, "post #" + i + ": " + post.toString());
            i++;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPostBinding binding = ItemPostBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post post = posts.get(position);
        holder.bind(post);
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        ItemPostBinding binding;

        public ViewHolder(@NonNull ItemPostBinding binding){
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(final Post post){
            binding.postCaption.setText(post.getCaption());
            binding.profileHandle.setText(post.getUser().getUsername());

            //Timestamp
            String relativeDate = DateUtility.getRelativeTimeAgo(post);
            binding.timestamp.setText(relativeDate);

            //Like status

            //Like numbers
            List<Like> likes = post.getLikes();
            int numOfLikes = likes.size();
            Resources res = context.getResources();

            if(numOfLikes > 0) {
                Like singleLike  = post.getLikes().get(0);
                ParseUser authorOfLike = singleLike.getAuthor();
                String nameAuthorOfLike = "";

                if(numOfLikes == 1){ // Only 1 like --> user likes this
                    try {
                        nameAuthorOfLike = authorOfLike.fetchIfNeeded().getString("username");
                        binding.likesText.setVisibility(View.VISIBLE);

                        String likeText = String.format(res.getString(R.string.likes), nameAuthorOfLike);
                        binding.likesText.setText(likeText);
                    }
                    catch (Exception e) {
                        Log.e(TAG, "Couldn't retrieve username: " + e);
                        binding.likesText.setVisibility(View.GONE);
                    }
                }
                else { //Multiple likes --> show 1 user and 'and # others like this'
                    try {
                        nameAuthorOfLike = authorOfLike.fetchIfNeeded().getString("username");
                        binding.likesText.setVisibility(View.VISIBLE);

                        String likeText = String.format(res.getString(R.string.multiple_likes), nameAuthorOfLike, numOfLikes-1);
                        binding.likesText.setText(likeText);
                    }
                    catch (Exception e) {
                        Log.e(TAG, "Couldn't retrieve username: " + e);
                        binding.likesText.setVisibility(View.GONE);
                    }
                }
            }
            else{ //No likes yet
                binding.likesText.setVisibility(View.GONE);
            }

            //User's profile picture
            ParseFile userProfilePicture = post.getUser().getParseFile("profilePicture");
            if(userProfilePicture != null){
                Glide.with(context)
                        .load(userProfilePicture.getUrl())
                        .circleCrop()
                        .into(binding.profilePicture);
            }
            else{
                Log.e(TAG,  "No profile picture found");
                Glide.with(context)
                        .load(R.drawable.default_profile_picture)
                        .circleCrop()
                        .into(binding.profilePicture);
            }

            //Post's image
            ParseFile postPhoto = post.getImage();
            if(postPhoto != null){
                Glide.with(context)
                        .load(postPhoto.getUrl())
                        .into(binding.postPhoto);
            }

            //LISTENERS

            binding.userContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent someonesProfileIntent = new Intent(context, SomeonesProfileActivity.class);

                    //Passing data to the intent
                    someonesProfileIntent.putExtra("post_object", Parcels.wrap(post));

                    context.startActivity(someonesProfileIntent);
                }
            });
        }
    }
}
