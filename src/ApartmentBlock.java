package src;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@XmlRootElement
@XmlAccessorType (XmlAccessType.FIELD)
public class ApartmentBlock {

    @XmlElement(name = "rest")
    //lista di case nella rete peer to peer
    private List<Home> homesList;
    //Map con id della casa come chiave e lista di statistiche locali come valore
    private Map<Integer, ArrayList<Statistics>> localStatisticsMap;
    //statistiche globali del condominio
    private List<Statistics> globalStatisticsList;
    private static ApartmentBlock instance;

    public ApartmentBlock() {
        homesList = new ArrayList<>();
        localStatisticsMap = new HashMap<>();
        globalStatisticsList = new ArrayList<>();
    }

    //singleton
    public synchronized static ApartmentBlock getInstance() {
        if(instance == null)
            instance = new ApartmentBlock();
        return instance;
    }

    public synchronized List<Home> getHomesList() {
        return new ArrayList<>(homesList);
    }

    public synchronized Map<Integer, ArrayList<Statistics>> getLocalStatisticsMap() {
        return new HashMap<>(localStatisticsMap);
    }

    public synchronized List<Statistics> getGlobalStatisticsList() {
        return new ArrayList<>(globalStatisticsList);
    }

    public synchronized List<Home> addHome(Home newHome){
        for(Home home: homesList)
            if(home.getId() == newHome.getId()) {
                System.out.println("addHome: casa con id " + home.getId() + " già presente");
                return null;
            }
        homesList.add(newHome);
        return getHomesList();
    }

    public synchronized void removeHome(int id)
    {
        for(Home home : getHomesList())
            if(home.getId() == id) {
                homesList.remove(home);
            }
    }

    public Home getHomeById(int id){
        List<Home> homeListCopy = getHomesList();
        for(Home home: homeListCopy)
            if(home.getId() == id)
                return home;
        return null;
    }

    public synchronized void addLocalStatistics(Statistics s) {
        int homeId = s.getHomeId();
        ArrayList<Statistics> statisticsList = localStatisticsMap.get(homeId);
        if(statisticsList == null)
            statisticsList = new ArrayList<>();
        statisticsList.add(s);
        localStatisticsMap.put(homeId, statisticsList);
    }

    public List<Statistics> getLocalStatistics(int homeId, int n)
    {
        ArrayList<Statistics> statisticsList = getLocalStatisticsMap().get(homeId);
        if(statisticsList == null)
            return new ArrayList<>();
        if(n > statisticsList.size())
            return statisticsList;
        else
            return statisticsList.subList(statisticsList.size() - n, statisticsList.size());
    }

    public synchronized void addGlobalStatistics(Statistics s) {
        globalStatisticsList.add(s);
    }

    public List<Statistics> getGlobalStatistics(int n)
    {
        List<Statistics> globalStatisticsListCopy = getGlobalStatisticsList();
        if(n > globalStatisticsListCopy.size())
            return globalStatisticsListCopy;
        else
            return globalStatisticsListCopy.subList(globalStatisticsListCopy.size() - n, globalStatisticsListCopy.size());
    }

    public double average(List<Statistics> statsList) {
        if (statsList.size() == 0)
            return 0;
        double average = 0;
        for(Statistics s : statsList)
            average = average + s.getValue();
        return average / statsList.size();
    }

    public double standardDeviation(List<Statistics> statsList) {
        if(statsList.size() == 0)
            return 0;
        double average = average(statsList);
        System.out.println(average);
        int N = statsList.size();
        System.out.println(N);
        double squareSum = 0;
        for (Statistics s : statsList)
            squareSum = squareSum + s.getValue() * s.getValue();
        System.out.println(squareSum);
        return Math.sqrt(1.0/N * squareSum - average * average);
    }

    public double getGlobalStatisticsAverage(int n) {
        List<Statistics> statsList = getGlobalStatistics(n);
        return average(statsList);
    }

    public double getLocalStatisticsAverage(int homeId, int n) {
        List<Statistics> statsList = getLocalStatistics(homeId, n);
        return average(statsList);
    }

    public double getGlobalStatisticsStandardDeviation(int n) {
        List<Statistics> statsList = getGlobalStatistics(n);
        return standardDeviation(statsList);
    }

    public double getLocalStatisticsStandardDeviation(int homeId, int n) {
        List<Statistics> statsList = getLocalStatistics(homeId, n);
        return standardDeviation(statsList);
    }

}