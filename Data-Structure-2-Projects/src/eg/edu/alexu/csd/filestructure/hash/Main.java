package eg.edu.alexu.csd.filestructure.hash;

import java.util.LinkedList;

public class Main {
  public static void main(String[] args) {
    int l = 0, q = 0, d = 0, s = 0;
    LinkedList<Integer> ll, qq, ss, dd;
    ll = new LinkedList<>();
    ss = new LinkedList<>();
    qq = new LinkedList<>();
    dd = new LinkedList<>();
    for (int i = 0; i < 5; i++) {
      HashTableLinearProbing<Integer, Integer> lin = new HashTableLinearProbing<>();
      HashTableQuadraticProbing<Integer, Integer> qua = new HashTableQuadraticProbing<>();
      HashTableDoubleHashing<Integer, Integer> dou = new HashTableDoubleHashing<>();
      HashTableChaining<Integer, Integer> sep = new HashTableChaining<>();
      for (int j = 0; j < 10000; j++) {
        int randKey = (int) (Math.random() * 10000.0);
        int randVal = (int) (Math.random() * 10000.0);
        lin.put(randKey, randVal);
        qua.put(randKey, randVal);
        dou.put(randKey, randVal);
        sep.put(randKey, randVal);
      }
      l += lin.collisions();
      q += qua.collisions();
      d += dou.collisions();
      s += sep.collisions();
      ll.add(lin.collisions());
      qq.add(qua.collisions());
      dd.add(dou.collisions());
      ss.add(sep.collisions());
    }
    for (Integer a : ll) {
      System.out.println(
          "Linear Probing Sample with random 10000 insert produce " + a + " Collisions \\\\");
    }
    System.out.println("{\\bf Linear Probing Average = " + l / 5 + " Collisions}");
    for (Integer a : qq) {
      System.out.println(
          "Quadratic Probing Sample with random 10000 insert produce " + a + " Collisions\\\\");
    }
    System.out.println("{\\bf Quadratic Probing Average = " + q / 5 + " Collisions}");
    for (Integer a : dd) {
      System.out.println(
          "Double Hashing Sample with random 10000 insert produce " + a + " Collisions\\\\");
    }
    System.out.println("{\\bf Double Hashing Average = " + d / 5 + " Collisions}");
    for (Integer a : ss) {
      System.out
          .println("Chaining Sample with random 10000 insert produce " + a + " Collisions\\\\");
    }
    System.out.println("{\\bf Chaining Average = " + s / 5 + " Collisions}");
  }
  
}
