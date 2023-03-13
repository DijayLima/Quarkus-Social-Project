package io.github.DijayLima.quarkussocial.rest;


import io.github.DijayLima.quarkussocial.domain.model.Post;
import io.github.DijayLima.quarkussocial.domain.model.User;
import io.github.DijayLima.quarkussocial.domain.repository.FollowerRepository;
import io.github.DijayLima.quarkussocial.domain.repository.PostRepository;
import io.github.DijayLima.quarkussocial.domain.repository.UserRepository;
import io.github.DijayLima.quarkussocial.rest.dto.FollowerRequest;
import io.github.DijayLima.quarkussocial.rest.dto.PostRequest;
import io.github.DijayLima.quarkussocial.rest.dto.PostResponse;
import io.quarkus.panache.common.Sort;
import org.jboss.resteasy.core.PostResourceMethodInvoker;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.stream.Collectors;

@Path("/users/{userId}/posts")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PostResource {

    private UserRepository userRepository;
    private PostRepository postRepository;
    private FollowerRepository followerRepository;

    @Inject
    public PostResource(UserRepository userRepository,
                        PostRepository postRepository,
                        FollowerRepository followerRepository) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.followerRepository = followerRepository;
    }

    @POST
    @Transactional
    public Response savePost(@PathParam("userId") Long userId, PostRequest request){
        User user = userRepository.findById(userId);
        if(user == null)
            return Response
                    .status(Response.Status.NOT_FOUND.getStatusCode())
                    .build();
        Post post = new Post();
        post.setText(request.getText());
        post.setUser(user);

        postRepository.persist(post);

        return Response
                .status(Response.Status.CREATED.getStatusCode())
                .build();
    }

    @GET
    public Response listPosts(@PathParam("userId") Long userId,
                              @HeaderParam("followerId") Long followerId){
        User user = userRepository.findById(userId);
        if(user == null)
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .build();

        if(followerId == null)
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity("You forget the header followerId")
                    .build();

        var follower = userRepository.findById(followerId);

        if(follower == null)
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity("Inexistent followerId")
                    .build();

        if(!followerRepository.follows(follower, user))
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("You can't see these posts")
                    .build();


        var list = postRepository.find("user",
                Sort.by("dateTime", Sort.Direction.Descending), user).list();

        var postResponseList = list.stream()
                .map(PostResponse::fromEntity)
                .collect(Collectors.toList());

        return Response
                .ok(postResponseList)
                .build();
    }

}
