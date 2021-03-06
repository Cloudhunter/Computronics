package pl.asie.computronics.util.cipher;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * @author Vexatos
 */
public class RSACalculationTask implements Callable<ArrayList<Map<Integer, String>>> {

	private static final BigInteger
		ONE = BigInteger.ONE,
		TWO = new BigInteger("2"),
		SEVENTEEN = new BigInteger("17");

	private final RSAValue val;
	private int bitLength = 0;
	private int p = 0;
	private int q = 0;
	private Map<Integer, String> publicKey;
	private Map<Integer, String> privateKey;

	public RSACalculationTask(RSAValue val) {
		this.val = val;
	}

	public RSACalculationTask(RSAValue val, int bitLength) {
		this(val);
		this.bitLength = bitLength;
	}

	public RSACalculationTask(RSAValue val, int p, int q) {
		this(val);
		this.p = p;
		this.q = q;
	}

	@Override
	public ArrayList<Map<Integer, String>> call() {
		if(bitLength > 0) {
			return this.createKeySet(bitLength);
		}
		if(p > 0 && q > 0) {
			return this.createKeySet(p, q);
		}
		return this.createKeySet();
	}

	private ArrayList<Map<Integer, String>> createKeySet() {
		SecureRandom r = new SecureRandom();
		return this.createKeySet(
			new BigInteger(1024, 100, r),
			new BigInteger(1024, 100, r));
	}

	private ArrayList<Map<Integer, String>> createKeySet(int bitLength) {
		SecureRandom r = new SecureRandom();
		return this.createKeySet(
			new BigInteger(bitLength, 100, r),
			new BigInteger(bitLength, 100, r));
	}

	private ArrayList<Map<Integer, String>> createKeySet(int p, int q) {
		return this.createKeySet(
			BigInteger.valueOf(p),
			BigInteger.valueOf(q));
	}

	private ArrayList<Map<Integer, String>> createKeySet(BigInteger p, BigInteger q) {
		BigInteger cat = p.subtract(ONE).multiply(q.subtract(ONE));
		BigInteger n = p.multiply(q);
		BigInteger d = SEVENTEEN;
		while(cat.gcd(d).intValue() != 1) {
			d = d.add(TWO);
		}
		BigInteger e = d.modInverse(cat);

		Map<Integer, String> publicKey = new LinkedHashMap<Integer, String>();
		Map<Integer, String> privateKey = new LinkedHashMap<Integer, String>();
		publicKey.put(1, n.toString());
		publicKey.put(2, d.toString());
		privateKey.put(1, n.toString());
		privateKey.put(2, e.toString());
		ArrayList<Map<Integer, String>> list = new ArrayList<Map<Integer, String>>();
		list.add(publicKey);
		list.add(privateKey);
		return list;
	}
}
