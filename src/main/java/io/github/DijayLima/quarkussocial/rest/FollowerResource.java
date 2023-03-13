package io.github.DijayLima.quarkussocial.rest;


import io.github.DijayLima.quarkussocial.domain.model.Follower;
import io.github.DijayLima.quarkussocial.domain.model.User;
import io.github.DijayLima.quarkussocial.domain.repository.FollowerRepository;
import io.github.DijayLima.quarkussocial.domain.repository.UserRepository;
import io.github.DijayLima.quarkussocial.rest.dto.FollowerPerUserResponse;
import io.github.DijayLima.quarkussocial.rest.dto.FollowerRequest;
import io.github.DijayLima.quarkussocial.rest.dto.FollowerResponse;
import io.github.DijayLima.quarkussocial.rest.dto.UserRequest;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.stream.Collectors;

@Path("/users/{userId}/followers")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class FollowerResource {

    private FollowerRepository followerRepository;
    private UserRepository userRepository;

    @Inject
    public FollowerResource(FollowerRepository followerRepository, UserRepository userRepository) {
        this.followerRepository = followerRepository;
        this.userRepository = userRepository;
    }

    @GET
    public Response listFollowers(@PathParam("userId") Long userId){
        User user = userRepository.findById(userId);
        if(user == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

         var list = followerRepository.findByUser(userId);
        FollowerPerUserResponse responseObject = new FollowerPerUserResponse();
        responseObject.setFollowersCount(list.size());

        var followerList = list.stream()
                .map(FollowerResponse::new)
                .collect(Collectors.toList());

        responseObject.setContent(followerList);
        return Response.ok(responseObject).build();
    }

    @DELETE
    @Transactional
    public Response unfollowUser(@PathParam("userId") Long userId,
                                 @QueryParam("followerId") Long followerId ){
        User user = userRepository.findById(userId);
        if(user == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        followerRepository.deleteByFollowerAndUser(followerId, userId);

        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @PUT
    @Transactional
    public Response followUser(@PathParam("userId") Long userId,
                               FollowerRequest request){

        if(userId.equals(request.getFollowerId())){
            return Response.status(Response.Status.CONFLICT)
                    .entity("You can\'t follow yourself!")
                    .build();
        }

        User user = userRepository.findById(userId);
        if(user == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        var follower = userRepository.findById(request.getFollowerId());

        if(!followerRepository.follows(follower, user)){
            var entity = new Follower();
            entity.setUser(user);
            entity.setFollower(follower);

            followerRepository.persist(entity);
        }

        return Response.status(Response.Status.NO_CONTENT).build();
    }
}
