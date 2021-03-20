/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Metier;

import Interface.MyFutoshikiGame;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.swing.JFrame;
import javax.swing.JTextField;

/**
 *
 * @author Hafsa GH
 */
// Pour trier des listes de (String, Integer) par ordre croissant de Integer
class cmpComptage implements Comparator {
    @Override
    public int compare(Object e1, Object e2) {
        return ((Map.Entry<String, Integer>)e1).getValue().compareTo(((Map.Entry<String, Integer>)e2).getValue());
    }
}

public class Backtracking {
    //static JTextField [][] maGrille;
    static int [][] valGrille; // Les valeurs
    static char [][] contraintesHoriz; // Grille des contraintes horizontales : < et >
    static char [][] contraintesVert; // Grille des contraintes verticales : ^ et v
    static ST<String, SET<String>> domain;
    // --- Variables boolean pour choix d'amélioration du Backtracking ---
    public static boolean withDEGREES = false;
    public static boolean withMRV = false;
    public static boolean withLCV = false;
    public static boolean withFC = false;
    public static boolean withAC1 = false;
    
    // --- Constructeur ---
    public Backtracking(MyFutoshikiGame game)
    {
        valGrille = game.getValGrille();
        contraintesHoriz = game.getContraintesHoriz();
        contraintesVert = game.getContraintesVert();
    }
    
    public static String getVariable(ST<String, String> config) {	
	//retrieve a variable based on a heuristic or the next 'unfilled' one if there is no heuristic
        for (String s : config) 
        {
            if(config.get(s).equalsIgnoreCase(""))
                return s;
        }	
        //get variable failed (all variables have been coloured)
        return null;
    }
	
    public static boolean complete(ST<String, String> config) {
        for(String s: config) {
            //if we find a variable in the config with no value, then this means that the config is NOT complete
            if(config.get(s).equalsIgnoreCase(""))
                return false;
        }
        //ALL variables in config have a value, so the configuration is complete
        return true;
    }
		
    public static boolean consistent(String value, String variable, ST<String, String> config, Graph g) {
        for(String adj: g.adjacentTo(variable)) 
        {
            if(!adj.contains("s") && !adj.contains("i")) // Si l'adjacent n'est ni supérieur ni inférieur
            {
                if(config.get(adj).equalsIgnoreCase(value))
                    return false;
            }
            else if(adj.contains("s")) // Si l'adjacent est supérieur
            {
                String nomSommet = adj.replace("s", "x"); // On remplace "s" par "x" pour pourvoir chercher dans le graphe (les sommets nommés "x")
                if(!config.get(nomSommet).equals(""))
                {
                    int variableNumber = Integer.parseInt(value);
                    int supNumber = Integer.parseInt(config.get(nomSommet));

                    if(supNumber <= variableNumber) // On compare avec la valeur à fournir
                        return false;
                }
            }
            else // Si l'adjacent est inférieur
            {
                String nomSommet = adj.replace("i", "x");
                if(!config.get(nomSommet).equals(""))
                {
                    int variableNumber = Integer.parseInt(value);
                    int infNumber = Integer.parseInt(config.get(nomSommet));

                    if(infNumber > variableNumber)
                        return false;
                }
            }
        }
        return true;
    }
	
    public static boolean consistent(String value, String variable, ST<String, String> config,
                                            ST<String, ST<String, ST<String, SET<String>>>> constraintsTable) {
        //we need to get the constraint list for the variable
        for(String constraints: constraintsTable.get(variable)) {
            //if the adjacency list member's value is equal to the variable's selected value, then consistency fails
            if(!config.get(constraints).equals("") && !(constraintsTable.get(constraints).get(value).contains(config.get(constraints)))) {
                return false;
            }
        }
        //consistency check passed according to the variable's adjacancy list
        return true;
    }
    
    static void aff(ST<String, String> config){
        System.out.println("");
        System.out.print(" - ");

        if(config ==null)
            System.out.print("Pas de solution");
        else
        {
            for (String s : config)
            {
               System.out.print("("+s + ", "+ config.get(s)+")");
            }
        }
    }
    
    /*---------------------- Degree ------------------------------*/
    public static String getVariableDegres( Graph g, ST<String, String> config)
    {
        // Stocker (variable, nombre de contraintes)
        TreeMap<String,Integer> compteParVariable = new TreeMap<>();
        // Table associative triée par ordre décroissant (à cause du - )
        for (String var : config)
            if(config.get(var).equalsIgnoreCase(""))
                compteParVariable.put(var, -g.degree(var)) ;
        // Mettre sous forme d'une liste puis trier
        List list = new ArrayList(compteParVariable.entrySet());
        Collections.sort(list, new cmpComptage());
        return ((Map.Entry<String, Integer>)list.get(0)).getKey();
    }
    /*---------------------- MVR ------------------------------*/
    public static String getVariableMRV(ST<String, SET<String>> domain , ST<String, String> config){
        // Stocker (variable, taille du domaine)
        TreeMap<String, Integer> compteParVariable = new TreeMap<>();
        // Table associative triée par ordre croissant
        for (String var : config)
            if(config.get(var).equalsIgnoreCase(""))
                compteParVariable.put(var,domain.get(var).size()) ;
        // Mettre sous forme d'une liste puis trier
        List list = new ArrayList(compteParVariable.entrySet());
        Collections.sort(list, new cmpComptage());
        return ((Map.Entry<String, Integer>)list.get(0)).getKey();
    }
    /*---------------------- Degree + MRV ------------------------------*/
    public static String getVariableDegresMRV(Graph g, ST<String, SET<String>> domain , ST<String, String> config)
    {
        // Stocker (variable, nombre de contraintes)
        TreeMap<String, Integer> compteParVariable1 = new TreeMap<>();
        // Stocker (variable, nombre de valeurs)
        TreeMap<String, Integer> compteParVariable2 = new TreeMap<>();
        // Table associative triée par ordre décroissant (à cause du - )
        for (String var : config)
            if(config.get(var).equalsIgnoreCase(""))
                compteParVariable1.put(var, -g.degree(var)) ;
        // Mettre sous forme d'une liste puis trier
        List list = new ArrayList(compteParVariable1.entrySet());
        Collections.sort(list, new cmpComptage());
        Integer compte0 = ((Map.Entry<String, Integer>)list.get(0)).getValue();
        Iterator it = list.iterator();
        // Garder les variables avec le nombre de degrés

        while(it.hasNext())
        {
            Map.Entry entree = (Map.Entry)it.next();
            if(((Integer)entree.getValue()).equals(compte0))
            {
                String var = (String)entree.getKey();
                compteParVariable2.put(var,domain.get(var).size());
            }
            else 
                break;
        }
        list = new ArrayList(compteParVariable2.entrySet());
        Collections.sort(list, new cmpComptage());
        return ((Map.Entry<String, Integer>)list.get(0)).getKey();
    }
    /*---------------------- Domaine d'une variable ------------------------------*/ 
    public static List<String> orderDomainValue(String variable, ST<String, SET<String>> domain) 
    {
        List<String> valeurs = new ArrayList<>();
        for(String val : domain.get(variable))
            valeurs.add(val);
        return valeurs;
    }
    /*---------------------- Domaine d'une variable avec LCV ------------------------------*/
    public static List<String> orderDomainValueLCV(String variable,Graph g, ST<String, SET<String>> domain) 
    {
    // Stocker (variable, nombre de contraintes)
    TreeMap< String, Integer> compteParValeur = new TreeMap<>();
    //return the SET of domain values for the variable
    SET<String> vu = domain.get(variable);
    for(String v:vu)
    {
        int n=1;
        for(String adj: g.adjacentTo(variable))
            if(domain.get(adj) != null && domain.get(adj).contains(v))
                n++;
        compteParValeur.put(v,n);
    }
    // Mettre sous forme d'une liste puis trier
    List list = new ArrayList(compteParValeur.entrySet());
    Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() 
    {
        @Override
        public int compare(Map.Entry<String, Integer> e1, Map.Entry<String, Integer> e2) 
        {
            return e1.getValue().compareTo(e2.getValue());
        }
    });
    // Liste des valeurs
    List<String> vals = new ArrayList<>();
    Iterator it = list.iterator();
    while(it.hasNext())
    {
        Map.Entry<String, Integer> entree = (Map.Entry<String, Integer>)it.next();
        vals.add((String)entree.getKey());
    }
    return vals;
    }
    /*---------------------- Forwardchecking ------------------------------*/
    public static SET<String> forwardChecking(String u , String variable , Graph g ,ST<String, String> config ,ST<String, SET<String>> domain )
    { 
        // Variables touchées
        SET<String> vars = new SET<>();
        for(String adj: g.adjacentTo(variable))
        {
            if(config.get(adj) != null && config.get(adj).equalsIgnoreCase("") && domain.get(adj).contains(u))
            {
                domain.get(adj).remove(u);
                vars.add(adj);
            }
        }
        return vars;
    } 
    /*---------------------- AC1 ------------------------------*/
    public static void AC1(Graph g, ST<String, String> config, ST<String, SET<String>> domain)
    {
        boolean changement;
        do 
        {
            changement = false;
            for(String variable : config)
            {
                if(config.get(variable).equalsIgnoreCase("")) // Pour chaque variable non affectée
                {
                    for(String adj : g.adjacentTo(variable))
                    {
                        if(config.get(adj).equalsIgnoreCase("")) // Adjacente non affectée
                        {
                            // Pour éviter l'erreur : Exception in thread "main"
                            // java.util.ConcurrentModificationException
                            SET<String> valeurs = new SET<>(domain.get(variable).getSet());
                            for(String val : valeurs)
                            {
                                SET<String> adjDomain = domain.get(adj);
                                // Valeur consistante introuvable
                                if((adjDomain != null) && (adjDomain.contains(val)) && (adjDomain.size() == 1))
                                {
                                    // Supprimer le domaine de la variable
                                    domain.get(variable).remove(val);
                                    changement = true;
                                }
                            }
                        }
                    }
                }
            }
        } while(changement);
    }
    
    public static ST<String, String> backtracking(ST<String, String> config, ST<String, SET<String>> domain, Graph g){
        // -------------------------------------- Backtracking simple --------------------------------------       
        //domain = domain;
        // Arrêter s'il s'agit d'une affectation complete
        if(complete(config))
                return config;
        
        ST<String, String> result = null;
        //String v = getVariable(config); // Backtracking simple
        //String v = MVR(domain, config); // En utilisant MVR
        
        // --- Variable à affecter ---
        String v = null;
        if(withMRV)
            v = getVariableMRV(domain, config);
        else if(withDEGREES)
            v = getVariableDegres(g, config);
        else if(withMRV && withDEGREES)
            v = getVariableDegresMRV(g, domain, config);
        else
            v = getVariable(config);
        // --- Liste des valeurs du domaine de la variable choisie ---
        List <String> vu;
        if(withLCV)
            vu = orderDomainValueLCV(v, g, domain);
        else
            vu = orderDomainValue(v, domain);
        // Domaine de cette variable (liste des valeurs)
        //SET<String> vu = orderDomainValue(v, domain);
        // Variables affectées par la vérification en aval
        SET<String> variablesTouchees = null;
        // Préparer la sauvegarde des domaines
        ST<String, SET<String>> tmpDomain = null;
        // Parcourir la liste des valeurs
        for(String u: vu) {
            if(consistent(u, v, config, g)) { // 
                config.put(v, u); //
                aff(config);
                // Sauvegarde des domaines
                if(withAC1 || withFC)
                {
                    tmpDomain = new ST<>();
                    for(String vr : domain)
                        tmpDomain.put(vr, new SET<>(domain.get(vr).getSet()));
                }
                if(withFC)
                    variablesTouchees = forwardChecking(u, v, g, config, domain);
                //--------------------------------------------------------
                if(withAC1 || withFC)
                    result = backtracking(config, tmpDomain, g);
                else
                    result = backtracking(config, domain, g);
                if(result != null)
                    return result;

                config.put(v,""); // X config.remove(v)
                if(withFC)
                    for(String var : variablesTouchees)
                        domain.get(var).add(u);
            }
        }
        return null;
    }
}
