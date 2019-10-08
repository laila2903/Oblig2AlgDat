import jdk.nashorn.internal.ir.annotations.Ignore;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

public class DobbeltLenketListe<T> implements Liste<T> {

    private static final class Node<T> {
        private T verdi;                   // nodens verdi
        private Node<T> forrige, neste;    // pekere

        private Node (T verdi, Node<T> forrige, Node<T> neste) {
            this.verdi = verdi;
            this.forrige = forrige;
            this.neste = neste;
        }

        private Node (T verdi) {
            this(verdi, null, null);
            this.verdi = verdi;
        }
    }


    // instansvariabler
    private Node<T> hode;          // peker til den første i listen
    private Node<T> hale;          // peker til den siste i listen
    private int antall;            // antall noder i listen
    private int endringer;         // antall endringer i listen


    public DobbeltLenketListe() {   //standardkonstruktør
        hode = hale = null;               // hode er null
        antall = 0;
        endringer = 0;
    }

    public DobbeltLenketListe(T[] a) {
        this(); //alle variablene er nullet
        Objects.requireNonNull(a, "Tabell a er null!");
        for (T verdi : a) {
            if (verdi != null) {
                a[antall++] = verdi; //hopper over null verdier
            }
        }

        for (int i = 0; i < a.length && a[i] == null; i++) {
            if (a.length < 2 && a != null) {
                Node<T> p = hode = hale = new Node<>(a[i], null, null);

            } else if (i < a.length) {
                Node<T> p = hode = new Node<>(a[i], null, null);
                antall = 1; //minst en node

                for (i++; i < a.length; i++) {
                    if (a[i] != null) {
                        p.neste = new Node<>(a[i], null, null); //en ny node
                        antall++;
                    }
                }
                hale = p;
            } else if (a.length < 1) {
                hode = hale = null;
                endringer = 0;
                antall = 0;
            }
        }
    }

    private static void fratilKontroll(int antall, int fra, int til) {
        if (fra < 0)                                  // fra er negativ
            throw new IndexOutOfBoundsException
                    ("fra(" + fra + ") er negativ!");

        if (til > antall)                          // til er utenfor tabellen
            throw new IndexOutOfBoundsException
                    ("til(" + til + ") > tablengde(" + antall + ")");

        if (fra > til)                                // fra er større enn til
            throw new IllegalArgumentException
                    ("fra(" + fra + ") > til(" + til + ") - illegalt intervall!");
    }

    public Liste<T> subliste(int fra, int til) {

        fratilKontroll(antall,fra,til);

        Node<T> p = finnNode(fra); //starter fra første node"fra"

        Liste<T> delliste = new DobbeltLenketListe<T>();

        for (int i = fra; i < til; i++) { //for-loop som løper gjennom alle nodene fra node fra til node til
            delliste.leggInn(p.verdi);  //legger inn verdiene i en egen subliste
            p = p.neste; // setter p = ny/neste p
        }

        return delliste;
    }

    @Override
    public int antall() {
        return antall;
    }

    @Override
    public boolean tom() {
        return antall() == 0;
    }

    @Override
    public boolean leggInn(T verdi) {
        Objects.requireNonNull(verdi, "Ikke tillat med null verdier");

        if (antall == 0) {
            hode = hale = new Node<>(verdi, null, null); //hvis antallet er null er hode=hale
        } else {
            hale = hale.neste = new Node<>(verdi, hale, null); //hvis antallet er mer enn null blir hale=1, så hale=2 (hale.neste)
        }
        antall++;
        endringer++;
        return true;
    }

    @Override
    public void leggInn(int indeks, T verdi) {
        Objects.requireNonNull(verdi, "Ikke tillatt med null-verdier!");

        indeksKontroll(indeks, true);  // Se Liste, true: indeks = antall er lovlig

        if (indeks < 0) {
            throw new IndexOutOfBoundsException("Indeks " +
                    indeks + " er negativ!");
        } else if (indeks > antall) {
            throw new IndexOutOfBoundsException("Indeks " +
                    indeks + " > antall(" + antall + ") noder!");
        } else if (antall == 0)  // tom liste
        {
            hode = hale = new Node<>(verdi, null, null);

        } else if (indeks == 0)  // ny verdi forrest
        {
            hode = hode.forrige = new Node<>(verdi, null, hode);

        } else if (indeks == antall)  // ny verdi bakerst
        {
            hale = hale.neste = new Node<>(verdi, hale, null);

        } else {
            Node<T> p = finnNode(indeks);  // ny verdi til venstre for p
            p.forrige = p.forrige.neste = new Node<>(verdi, p.forrige, p);
        }

        antall++;      // ny verdi i listen
        endringer++;   // en endring i listen
    }

    @Override
    public boolean inneholder(T verdi) {
        return indeksTil(verdi) != -1;  //gir sann hvis den inneholder verdi. -1 betyr indeks utenfor.
    }

    private Node<T> finnNode(int indeks) {
        Node<T> p;

        if (indeks <= antall / 2) {  //hvis indeksen er mindre enn antall/2 vil den lete fra hodet og til midten
            p = hode;
            for (int i = 0; i < indeks; i++) {
                p = p.neste;
            }
        } else {
            p = hale;  //hvis indeksen er større enn antall/2 vil den lete fra hale og til midten
            for (int i = antall - 1; i > indeks; i--) {
                p = p.forrige;
            }
        }
        return p;
    }

    @Override
    public T hent(int indeks) {  //skal hente (uten å fjerne) verdien på indeks, bruker fratilkontroll for å sjekke indeksene
        indeksKontroll(indeks, false);
        return finnNode(indeks).verdi;
    }

    @Override
    public int indeksTil(T verdi) {  //returner plassen/indeksen til første forekomst av verdi.

        Node<T> p = hode;

        for (int indeks = 0; indeks < antall; indeks++) {
            if (p.verdi.equals(verdi)) {  //setter verdien lik p sin verdi og returnerer indeksen
                return indeks;
            }
            p = p.neste;
        }
        return -1; //returnerer -1 hvis verdine ikke finnes
    }

    @Override
    public T oppdater(int indeks, T nyverdi) {  //erstatter eksisterende verdi på indeks med verdien verdi.
        Objects.requireNonNull(nyverdi, "Ikke tillatt med null-verdier!");

        indeksKontroll(indeks, false);

        Node<T> p = finnNode(indeks);

        T gammelVerdi = p.verdi; //første verdien er nåværende verdi

        p.verdi = nyverdi; //setter nåværende verdi til ny eller andre verdi
        endringer++;
        return gammelVerdi; //returnerer den gamle verdien
    }

    @Override
    public boolean fjern(T verdi) {  //skal fjerne første forekomst av verdi og returnere sann

        if (verdi == null) return false; //returnerer false hvis verdien er lik null (ingenting å fjerne da)

        Node<T> p = hode;

        while (p != null)  // ser etter verdien
        {
            if (p.verdi.equals(verdi))
                break;
            p = p.neste;
        }

        if (p == null) {
            return false;        // verdi er ikke i listen
        }
        else if (antall == 1)  // bare en node i listen
        {
            hode = hale = null;
        }
        else if (p == hode)    // fjerner den første
        {
            hode = hode.neste;
            hode.forrige = null;
        }
        else if (p == hale)    // fjerner den siste
        {
            hale = hale.forrige;
            hale.neste = null;
        }
        else {                         //alle verdiene som kommer etterpå får redusert sin indeks med 1
            p.forrige.neste = p.neste; //den forrige sin neste blir nå den neste
            p.neste.forrige = p.forrige; //den neste sin forrige blir nå den forrige
        }

        p.verdi = null;              // for resirkulering
        p.forrige = p.neste = null;  // for resirkulering

        antall--;
        endringer++;

        return true;
    }

    @Override
    public T fjern(int indeks) {  //skal fjerne (og returnere) verdien med denne indeksen

        indeksKontroll(indeks, false);

        Node<T> p = hode;

        if (antall == 1)  // bare en node i listen
        {
            hode = hale = null; //fjerner hele noden
        }
        else if (indeks == 0)  // den første skal fjernes
        {
            hode = hode.neste;
            hode.forrige = null;  //forflytter hodet til neste og sletter da den som blir den forrige
        }
        else if (indeks == antall - 1)  // den siste skal fjernes
        {
            p = hale;
            hale = hale.forrige;
            hale.neste = null;   //forflytter hale fra den siste til neste siste og sletter den siste
        }
        else {
            p = finnNode(indeks);
            p.forrige.neste = p.neste;
            p.neste.forrige = p.forrige;
        }

        T verdi = p.verdi;           // skal returneres

        p.verdi = null;              // for resirkulering
        p.forrige = p.neste = null;  // for resirkulering

        antall--;
        endringer++;

        return verdi;
    }

    @Override
    public void nullstill() {  //tømmer listen og resirkulerer hva enn som blir igjen

        Node<T> p = hode;
        Node<T> q = null;

        while (p != null) {
            q = p.neste;
            p.neste = null;
            p.verdi = null;
            p = q;
        }
        hode = hale = null;
        antall = 0;
    }

    @Override
    public String toString() {

        StringBuilder s = new StringBuilder();

        if(tom() || hode.verdi == null){
            return "[]";
        }

        s.append("[");
        if(!tom()) {

            Node<T> p = hode;
            s.append(p.verdi);

            p=p.neste;

            while (p!=null){
                s.append(",").append(" ").append(p.verdi);
                p=p.neste;
            }
        }
        s.append("]");
        return s.toString();

    }


   public String omvendtString() {

       if(tom() || hode.verdi == null){
           return "[]";
       }

        StringBuilder s = new StringBuilder();

       s.append("[");
       if(!tom()) {

           Node<T> p = hale;
           s.append(p.verdi);

           p=p.forrige;

           while (p!=null){
               s.append(",").append(" ").append(p.verdi);
               p=p.forrige;
           }
       }
       s.append("]");
       return s.toString();

    }

    @Override
    public Iterator<T> iterator() {
        return new DobbeltLenketListeIterator(); //returnerer instans av iteratorklassen
    }

    public Iterator<T> iterator(int indeks) {
        indeksKontroll(indeks, false); //sjekker om indeksen er lovlig
        return new DobbeltLenketListeIterator(indeks); //returnerer instans av iteratorklassen
    }

    private class DobbeltLenketListeIterator implements Iterator<T> {
        private Node<T> denne;
        private boolean fjernOK;
        private int iteratorendringer;

        private DobbeltLenketListeIterator() {
            denne = hode;     // denne starter på den første i listen
            fjernOK = false;  // blir sann når next() kalles
            iteratorendringer = endringer;  // teller endringer
        }

        private DobbeltLenketListeIterator(int indeks) {
            Node<T> p = finnNode(indeks);  //finner noden
            this.denne = p.neste; //setter pekeren denne til noden
        }

        @Override
        public boolean hasNext() {
            return denne != null;
        }

        @Override
        public T next() {

            Node<T> p = hode;

            if (iteratorendringer != endringer) {
                throw new ConcurrentModificationException("Listen er endret!");   //hvis iterator endring ikke er lik endring
            }

            if (!hasNext()) {
                throw new NoSuchElementException("Ingen verdier!"); //kaster dette hvis det ikke er noe elementer igjen
            }

            fjernOK = true;            // kaller nå remove()
            T thisVerdi = p.verdi;    // tar vare på verdien i p

            p = p.neste;               // flytter p til den neste node

            endringer++;
            iteratorendringer++;

            return thisVerdi;         // returnerer verdien
        }

        @Override
        public void remove() {
            if (!fjernOK){
                throw new IllegalStateException("Kan ikke fjerne en verdi nå!");
            }

            if (iteratorendringer != endringer) {
                throw new ConcurrentModificationException("Listen har blitt endret!");
            }

            fjernOK = false;

            Node<T> q = hode;
            Node<T> p = hode;

            if (antall == 1)    // bare en node i listen
            {
                hode = hale = null;
            }
            else if (p == null)  // den siste skal fjernes
            {
                q = hale;
                hale = hale.forrige;
                hale.neste = null;
            }
            else if (p.forrige == hode)  // den første skal fjernes
            {
                hode = hode.neste;
                hode.forrige = null;
            }
            else {
                q = p.forrige;  // q skal fjernes
                q.forrige.neste = q.neste;
                q.neste.forrige = q.forrige;
            }

            q.verdi = null;              // for resirkulering
            q.forrige = q.neste = null;  // for resirkulering

            antall--;             // en node mindre i listen
            endringer++;          // en endring i listen
            iteratorendringer++;  // en endring i iteratoren
        }

    } // class DobbeltLenketListeIterator

    public static <T> void sorter(Liste<T> liste, Comparator<? super T> c) {
        throw new NotImplementedException();
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static void main(String[] args) {

        String [] s1 = {};
        String [] s2 = {"A"};
        String [] s3 = {null,"A",null,"B",null};

        Liste<String> l1 = new DobbeltLenketListe<>(s1);
        Liste<String> l2 = new DobbeltLenketListe<>(s2);
        Liste<String> l3 = new DobbeltLenketListe<>(s3);

        System.out.println(l1.toString()+((DobbeltLenketListe<String>) l1).omvendtString()+l2.toString()+((DobbeltLenketListe<String>) l2).omvendtString()+l3.toString()+((DobbeltLenketListe<String>) l3).omvendtString());






    }
}
// class DobbeltLenketListe

