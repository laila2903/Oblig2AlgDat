import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.*;

public class DobbeltLenketListe<T> implements Liste<T> {

        /**
         * Node class
         * @param <T>
         */
        private static final class Node<T> {
            private T verdi;                   // nodens verdi
            private Node<T> forrige, neste;    // pekere

            private Node(T verdi, Node<T> forrige, Node<T> neste) {
                this.verdi = verdi;
                this.forrige = forrige;
                this.neste = neste;
            }

            private Node(T verdi) {
                this (verdi, null, null);
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
            for (T verdi: a){
                if(verdi != null){
                    a[antall++]=verdi; //hopper over null verdier
                }
            }

            for (int i = 0; i <a.length && a[i]==null; i++){
                if (a.length<2 && a !=null){
                    Node <T> p = hode = hale = new Node<>(a[i],null,null);

                }
                else if (i< a.length){
                    Node <T> p = hode = new Node <> (a[i],null,null);
                    antall = 1; //minst en node

                    for (i++; i<a.length;i++){
                        if (a[i] != null){
                            p.neste = new Node<>(a[i],null,null); //en ny node
                            antall++;
                        }
                    }
                    hale = p;
                }
                else if (a.length<1){
                    hode = hale = null;
                    endringer = 0;
                    antall = 0;
                }
            }
        }

        public Liste<T> subliste(int fra, int til){
            throw new NotImplementedException();
        }

        @Override
        public int antall() {
            return antall;
        }

        @Override
        public boolean tom() {
            return antall()==0;
        }

        @Override
        public boolean leggInn(T verdi) {
            Objects.requireNonNull(verdi,"Ikke tillat med null verdier");

            if(antall==0){
                hode=hale=new Node<>(verdi,null,null);
            }
            else{
                hale = hale.neste = new Node<>(verdi,hale,null);
            }
            antall++;
            endringer++;
            return true;
        }

        @Override
        public void leggInn(int indeks, T verdi) {
            Objects.requireNonNull(verdi, "Ikke tillatt med null-verdier!");

            indeksKontroll(indeks, true);  // Se Liste, true: indeks = antall er lovlig

            if (indeks < 0)
            {
                throw new IndexOutOfBoundsException("Indeks " +
                        indeks + " er negativ!");
            }
            else if (indeks > antall)
            {
                throw new IndexOutOfBoundsException("Indeks " +
                        indeks + " > antall(" + antall + ") noder!");
            }
            else if (antall == 0)  // tom liste
            {
                hode = hale = new Node<>(verdi, null, null);
            }
            else if (indeks == 0)  // ny verdi forrest
            {
                hode = hode.forrige = new Node<>(verdi, null, hode);
            }
            else if (indeks == antall)  // ny verdi bakerst
            {
                hale = hale.neste = new Node<>(verdi, hale, null);
            }
            else
            {
                Node<T> p = finnNode(indeks);  // ny verdi til venstre for p
                p.forrige = p.forrige.neste = new Node<>(verdi, p.forrige, p);
            }

            antall++;      // ny verdi i listen
            endringer++;   // en endring i listen
        }

        @Override
        public boolean inneholder(T verdi) {
            return indeksTil(verdi) !=-1;
        }

        private Node<T> finnNode(int indeks)
        {
            Node<T> p;

            if (indeks <= antall / 2) {
                p = hode;
                for (int i = 0; i < indeks; i++)
                {
                    p = p.neste;
                }
            }
            else {
                p = hale;
                for (int i = antall - 1; i > indeks; i--)
                {
                    p = p.forrige;
                }
            }
            return p;
        }

        @Override
        public T hent(int indeks) {
            indeksKontroll(indeks, false);  // Se Liste, false: indeks = antall er ulovlig
            return finnNode(indeks).verdi;
        }

        @Override
        public int indeksTil(T verdi) {
            if (verdi == null) {
                return -1;
            }

            Node<T> p = hode;

            for (int indeks = 0; indeks < antall ; indeks++)
            {
                if (p.verdi.equals(verdi)){
                    return indeks;
                }
                //p = p.neste;
            }
            return -1;
        }

        @Override
        public T oppdater(int indeks, T nyverdi) {
            Objects.requireNonNull(nyverdi, "Ikke tillatt med null-verdier!");

            indeksKontroll(indeks, false);  // Se Liste, false: indeks = antall er ulovlig

            Node<T> p = finnNode(indeks);
            T gammelVerdi = p.verdi;

            p.verdi = nyverdi;
            endringer++;
            return gammelVerdi;
        }

        @Override
        public boolean fjern(T verdi) {
            if (verdi == null) return false;

            Node<T> p = hode;

            while (p != null)  // leter etter verdien
            {
                if (p.verdi.equals(verdi)) break;
                p = p.neste;
            }

            if (p == null)
            {
                return false;        // verdi er ikke i listen
            }
            else if (antall == 1)  // bare en node i listen
            {
                hode = hale = null;
            }
            else if (p == hode)    // den første skal fjernes
            {
                hode = hode.neste;
                hode.forrige = null;
            }
            else if (p == hale)    // siste skal fjernes
            {
                hale = hale.forrige;
                hale.neste = null;
            }
            else
            {
                p.forrige.neste = p.neste;
                p.neste.forrige = p.forrige;
            }

            p.verdi = null;              // for resirkulering
            p.forrige = p.neste = null;  // for resirkulering

            antall--;      // en verdi mindre i listen
            endringer++;   // ny endring i listen

            return true;   // vellykket fjerning
        }

        @Override
        public T fjern(int indeks) {
            indeksKontroll(indeks, false);  // Se Liste, false: indeks = antall er ulovlig

            Node<T> p = hode;

            if (antall == 1)  // bare en node i listen
            {
                hode = hale = null; //eliminerer hele noden
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
            else
            {
                p = finnNode(indeks);  // bruker hjelpemetode
                p.forrige.neste = p.neste;
                p.neste.forrige = p.forrige;
            }

            T verdi = p.verdi;           // skal returneres
            p.verdi = null;              // for resirkulering
            p.forrige = p.neste = null;  // for resirkulering

            antall--;      // en verdi mindre i listen
            endringer++;   // ny endring i listen

            return verdi;
        }

        @Override
        public void nullstill() {
            Node <T> p = hode;
            Node <T> q = null;

            while (p!=null){
                q=p.neste;
                p.neste=null;
                p.verdi=null;
                p=q;
            }
            hode = hale = null;
            antall = 0;
        }

        @Override
        public String toString() {
            if(antall==0){
                return "[]";
            }
            StringBuilder s = new StringBuilder();

            s.append('[');

            if (!tom())
            {
                Node<T> p = hode;
                s.append(p.verdi);

                p = p.neste;

                while (p != null)  // tar med resten hvis det er noe mer
                {
                    s.append(',').append(' ').append(p.verdi);
                    p = p.neste;
                }
            }
            s.append(']');
            return s.toString();
        }

        public String omvendtString() {
            if(antall==0){
                return "[]";
            }
            StringBuilder s = new StringBuilder();

            s.append('[');

            if (!tom())
            {
                Node<T> p = hale;
                s.append(p.verdi);

                p = p.forrige;

                while (p != null)  // tar med resten hvis det er noe mer
                {
                    s.append(',').append(' ').append(p.verdi);
                    p = p.forrige;
                }
            }
            s.append(']');
            return s.toString();
        }

        @Override
        public Iterator<T> iterator() {
            return new DobbeltLenketListeIterator();
        }

        public Iterator<T> iterator(int indeks) {
            indeksKontroll(indeks,false);
            return new DobbeltLenketListeIterator(indeks);
        }

        private class DobbeltLenketListeIterator implements Iterator<T>
        {
            private Node<T> denne;
            private boolean fjernOK;
            private int iteratorendringer;

            private DobbeltLenketListeIterator(){
                throw new NotImplementedException();
            }

            private DobbeltLenketListeIterator(int indeks) {
                Node <T> p = finnNode(indeks);
                this.denne = p.neste;
            }

            @Override
            public boolean hasNext() { throw new NotImplementedException(); }

            @Override
            public T next(){

                Node <T> p = hode;

                if (iteratorendringer!=endringer){
                    throw new ConcurrentModificationException("Listen er endret!");
                }

                if (!hasNext()){
                    throw new NoSuchElementException("Ingen verdier!");
                }

                fjernOK = true;            // nå kan remove() kalles
                T denneVerdi = p.verdi;    // tar vare på verdien i p
                p = p.neste;               // flytter p til den neste noden

                endringer++;
                iteratorendringer++;
                return denneVerdi;         // returnerer verdien
            }

            @Override
            public void remove(){
                if (!fjernOK) throw
                        new IllegalStateException("Kan ikke fjerne en verdi nå!");

                if (iteratorendringer != endringer) throw
                        new ConcurrentModificationException("Listen har blitt endret!");

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
                else
                {
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

      Liste <String> liste = new DobbeltLenketListe<>();
      System.out.println(liste.antall() + " "+liste.tom());

    }
} // class DobbeltLenketListe

