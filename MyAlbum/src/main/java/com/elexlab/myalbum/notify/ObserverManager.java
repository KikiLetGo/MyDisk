package com.elexlab.myalbum.notify;

import java.util.HashSet;
import java.util.Set;

/**
 * Observer manager,we regist observer,unregist observer and
 * notify observer here
 * Created by BruceYoung on 15/11/16.
 */
public class ObserverManager {
    private static  ObserverManager instance = new ObserverManager();
    public static ObserverManager getInstance(){
         return  instance;
    }
    private  ObserverManager(){

    }
    //all observer
    private static Set<ObserverInterf> notifySet=new HashSet<ObserverInterf>();

    public synchronized void regist(ObserverInterf observer){
        if(!notifySet.contains(observer)) {
            notifySet.add(observer);
        }
    }
    public synchronized void unregist(ObserverInterf observer){
        notifySet.remove(observer);
    }

    //notify observers when the data changed
    public synchronized void notify(int type,long param1,long param2,Object obj){
        for(ObserverInterf observer:notifySet){
            if(observer.onDataChange(type,param1,param2,obj)){
                break;
            }
        }
    }

}
