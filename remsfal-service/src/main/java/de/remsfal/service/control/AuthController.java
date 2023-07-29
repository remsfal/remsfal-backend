package de.remsfal.service.control;

import antlr.Token;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.gson.Gson;
import de.remsfal.core.dto.ImmutableUserJson;
import de.remsfal.core.dto.UserJson;
import de.remsfal.core.model.CustomerModel;
import de.remsfal.core.model.UserModel;
import de.remsfal.service.boundary.authentication.TokenInfo;
import de.remsfal.service.boundary.authentication.TokenStore;
import de.remsfal.service.entity.dao.ProjectRepository;
import de.remsfal.service.entity.dao.UserRepository;
import de.remsfal.service.entity.dto.ProjectMembershipEntity;
import de.remsfal.service.entity.dto.UserEntity;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.persistence.NoResultException;

@ApplicationScoped

public class AuthController {

    @Inject
    ProjectRepository projectRepository;

    @Inject
    UserController userController;
    @Inject
    @ConfigProperty(name = "jwt.secret")
    String jwtSecret;

    @Inject
    TokenStore tokenStore;

    public String generateJWT(TokenInfo tokenInfo) {

        Algorithm algorithm = Algorithm.HMAC256(jwtSecret);
        Gson gson = new Gson();
        ArrayList<String> proprietorProjects = new ArrayList<>();
        ArrayList<String> managerProjects = new ArrayList<>();
        ArrayList<String> lessorProjects = new ArrayList<>();
        ArrayList<String> caretakerProjects = new ArrayList<>();
        ArrayList<String> consultantProjects = new ArrayList<>();
        ArrayList<String> lesseeProjects = new ArrayList<>();
        CustomerModel user = null;
        try {
            user = userController.getUserByTokenId(tokenInfo.getId());
        } catch (NoResultException e) {
            user = userController.createUser(tokenInfo);
        }

        List<ProjectMembershipEntity>  projectMembershipEntities = projectRepository.findMembershipByUserId(user.getId());
        for (ProjectMembershipEntity entity : projectMembershipEntities) {
            System.out.println("ID: " + entity.getId());
            System.out.println("Project: " + entity.getProject());
            System.out.println("User: " + entity.getUser());
            System.out.println("Role: " + entity.getRole());
            System.out.println("Email: " + entity.getEmail());
            System.out.println("-----------");

            switch(entity.getRole().toString()){
                case "PROPRIETOR":
                    proprietorProjects.add(entity.getProject().getId());
                    break;
                case "MANAGER":
                    managerProjects.add(entity.getProject().getId());
                    break;
                case "LESSOR":
                    lessorProjects.add(entity.getProject().getId());
                    break;
                case "CARETAKER":
                    caretakerProjects.add(entity.getProject().getId());
                    break;
                case "CONSULTANT":
                    consultantProjects.add(entity.getProject().getId());
                    break;
                case "LESSEE":
                    lesseeProjects.add(entity.getProject().getId());
                    break;
                default:
                    System.out.println("Invalid role");
            }
        }


        String token = JWT.create()
                .withIssuer("remsfal")
                .withClaim("email", user.getEmail())
                .withClaim("sub", user.getId())
                .withClaim("proprietorProjects", gson.toJson(proprietorProjects))
                .withClaim("managerProjects", gson.toJson(managerProjects))
                .withClaim("lessorProjects", gson.toJson(lessorProjects))
                .withClaim("caretakerProjects", gson.toJson(caretakerProjects))
                .withClaim("consultantProjects", gson.toJson(consultantProjects))
                .withClaim("lesseeProjects", gson.toJson(lesseeProjects))
                .sign(algorithm);
        setJwt(token);
        return token;
    }

    public void setJwt(String jwt) {
        tokenStore.setJwt(jwt);
    }

    public String getJwt() {
        return tokenStore.getJwt();
    }


    public boolean isAdminForProject(String projectId, String token){
        DecodedJWT jwt = getDecodedJWT(token);
        if(jwt != null){
            Claim claim = jwt.getClaim("managerProjects");
            if(claim != null){
                Gson gson = new Gson();
                ArrayList<String> managerProjects = gson.fromJson(claim.asString(), ArrayList.class);
                if(managerProjects.contains(projectId)){
                    return true;
                }
            }
        }
        return false;
    }


    public DecodedJWT getDecodedJWT(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(jwtSecret);
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer("remsfal")
                    .build(); //Reusable verifier instance
            DecodedJWT jwt = verifier.verify(token);
            return jwt;
        } catch (JWTVerificationException exception){
            // Invalid signature/claims
            System.out.println("Invalid token");
            return null;
        }
    }

}
