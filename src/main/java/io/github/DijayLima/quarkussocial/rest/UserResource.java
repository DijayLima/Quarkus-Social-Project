package io.github.DijayLima.quarkussocial.rest;

import io.github.DijayLima.quarkussocial.domain.model.User;
import io.github.DijayLima.quarkussocial.domain.repository.UserRepository;
import io.github.DijayLima.quarkussocial.rest.dto.ResponseError;
import io.github.DijayLima.quarkussocial.rest.dto.UserRequest;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Set;

@Path("/users")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UserResource {

    private UserRepository repository;
    private Validator validator;

    @Inject
    public UserResource(UserRepository repository, Validator validator) {
        this.repository = repository;
        this.validator = validator;
    }

    @POST
    @Transactional
    public Response createUser(UserRequest userRequest){

        Set<ConstraintViolation<UserRequest>> violations = validator.validate(userRequest);

        if(!violations.isEmpty()){

            return ResponseError
                    .createFromValidation(violations)
                    .withStatusCode(ResponseError.UNPROCESSABLE_ENTITY_STATUS);
        }

        User user = new User();
        user.setName(userRequest.getName());
        user.setAge(userRequest.getAge());

        repository.persist(user);

        return Response.status(Response.Status.CREATED.getStatusCode())
                .entity(user)
                .build();
    }

    @GET
    public Response listAllUser(){
        return Response.ok(repository.findAll().list()).build();
    }

    @DELETE
    @Path("{id}")
    @Transactional
    public Response deleteUsers(@PathParam("id") Long id){
        User user = repository.findById(id);
        if(user != null) {
            repository.delete(user);
            return Response.noContent().build();
        }

        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @PUT
    @Path("{id}")
    @Transactional
    public Response updateUser(@PathParam("id") Long id, UserRequest userRequest){
        User user = repository.findById(id);
        if(user != null) {
            user.setName(userRequest.getName());
            user.setAge(userRequest.getAge());
            return Response.noContent().build();
        }

        return Response.status(Response.Status.NOT_FOUND).build();
    }
}
