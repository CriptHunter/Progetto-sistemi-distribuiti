package server;


import javax.ws.rs.*;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Path("rest")
public class HomesService {
    //inserisce una casa dato id, ip, porta
    @Path("home/add")
    @POST
    @Consumes({"application/json", "application/xml"})
    @Produces({"application/json", "application/xml"})
    public Response addHome(Home home){

        List<Home> homeList = ApartmentBlock.getInstance().addHome(home);
        if (homeList != null) {
            GenericEntity< List<Home> > entity;
            entity  = new GenericEntity< List<Home>>(homeList){};
            return Response.ok(entity).build();
        }
        else
            return Response.status(Response.Status.PRECONDITION_FAILED).build();
    }

    //rimuove una casa data una casa
    @Path("home/remove/{id}")
    @POST
    @Consumes({"application/json", "application/xml"})
    public Response removeHome(@PathParam("id") int id){
        ApartmentBlock.getInstance().removeHome(id);
        return Response.ok().build();
    }

    @Path("home/get")
    @GET
    @Produces({"application/json", "application/xml"})
    public Response getHomesList() {
        List<Home> homeList = ApartmentBlock.getInstance().getHomesList();
        if(homeList.size() > 0)
            return Response.ok(homeList).build();
        else
            return Response.status(Response.Status.NOT_FOUND).build();
    }

    //ritorna casa dato id
    @Path("home/get/{id}")
    @GET
    @Produces({"application/json", "application/xml"})
    public Response getHomeById(@PathParam("id") int id){
        Home home = ApartmentBlock.getInstance().getHomeById(id);
        if(home != null)
            return Response.ok(home).build();
        else
            return Response.status(Response.Status.NOT_FOUND).build();
    }

    //aggiunge statistica di una casa, l'id della casa è nella statistica
    @Path("stats/local/add")
    @POST
    @Consumes({"application/json", "application/xml"})
    public Response addLocalStatistic(Statistics s)
    {
        ApartmentBlock.getInstance().addLocalStatistics(s);
        return Response.ok().build();
    }

    //ritorna le ultime n statistiche della casa con identificatore id
    @Path("stats/local/get/{id}/{n}")
    @GET
    @Produces({"application/json", "application/xml"})
    public Response getlocalstatistics(@PathParam("id") int id, @PathParam("n") int n){
        List<Statistics> s = ApartmentBlock.getInstance().getLocalStatistics(id, n);
        if(s.size() > 0)
            return Response.ok(s).build();
        else
            return Response.status(Response.Status.NOT_FOUND).build();
    }

    //ritorna le ultime n statistiche della casa con identificatore id
    @Path("stats/local/get/average/{id}/{n}")
    @GET
    @Produces({"application/json", "application/xml"})
    public Response getLocalStatisticsAverage(@PathParam("id") int id, @PathParam("n") int n){
        double average = ApartmentBlock.getInstance().getLocalStatisticsAverage(id, n);
        return Response.ok(average).build();
    }

    @Path("stats/local/get/std/{id}/{n}")
    @GET
    @Produces({"application/json", "application/xml"})
    public Response getLocalStandardDeviation(@PathParam("id") int id, @PathParam("n") int n) {
        double std = ApartmentBlock.getInstance().getLocalStatisticsStandardDeviation(id, n);
        return Response.ok(std).build();
    }

    //aggiunge statistica globale del condominio
    @Path("stats/global/add")
    @POST
    @Consumes({"application/json", "application/xml"})
    public Response addGlobalStatistics(Statistics s) {
        ApartmentBlock.getInstance().addGlobalStatistics(s);
        return Response.ok().build();
    }

    //ritorna le ultime n statistiche del condominio
    @Path("stats/global/get/{n}")
    @GET
    @Produces({"application/json", "application/xml"})
    public Response getGlobalStatics(@PathParam("n") int n) {
        List<Statistics> s = ApartmentBlock.getInstance().getGlobalStatistics(n);
        if (s.size() > 0)
            return Response.ok(s).build();
        else
            return Response.status(Response.Status.NOT_FOUND).build();
    }

    @Path("stats/global/get/average/{n}")
    @GET
    @Produces({"application/json", "application/xml"})
    public Response getGlobalStatisticsAverage (@PathParam("n") int n) {
        double average = ApartmentBlock.getInstance().getGlobalStatisticsAverage(n);
        return Response.ok(average).build();
    }

    @Path("stats/global/get/std/{n}")
    @GET
    @Produces({"application/json", "application/xml"})
    public Response getGlobalStandardDeviation(@PathParam("n") int n) {
        double std = ApartmentBlock.getInstance().getGlobalStatisticsStandardDeviation(n);
        return Response.ok(std).build();
    }
}