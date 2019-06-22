package Messages;

public enum Header {
    NET_ENTRANCE,     //annuncio entrata nella rete
    NET_ENTRANCE_ACK, //messaggio net_entrance ricevuto
    NET_EXIT,         //annuncio uscita dalla rete
    NET_EXIT_ACK,     //messaggio net_exit ricevuto
    LOCAL_STAT,       //nuova statistica locale
    LOCAL_STAT_COORD,  //statistica locale inviata dal coordinatore
    GLOBAL_STAT,      //nuova statistica globale
    BOOST_REQUEST,    //richiesta di boost
    BOOST_OK          //consenso sul boost
}