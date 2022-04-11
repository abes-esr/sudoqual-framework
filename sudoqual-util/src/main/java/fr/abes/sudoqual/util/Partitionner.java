package fr.abes.sudoqual.util;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * A partition of terms. A partition can be associated with a substition : two
 * terms are in the same class if one is the image of the other ex: the
 * partition associated with {(a,b)(c,b)(d,e)(e,f)} is {{a,b,c}{d,e,f}}
 */

public class Partitionner<T> implements Iterable<List<T>> {

	 List<List<T>> partition;

	// //////////////////////////////////////////////////////////////////////////
	// CONSTRUCTORS
	// //////////////////////////////////////////////////////////////////////////

	public Partitionner() {
		this.partition = new ArrayList<>();
	}

	/**
	 * Copy constructor
	 *
	 * @param partition
	 */
	public Partitionner(Partitionner<T> partition) {
		this();
		for (Collection<T> clazz : partition) {
			this.partition.add(new ArrayList<T>(clazz));
		}
	}

	/**
	 * Create a partition based on the position of elements in the two lists.
	 *
	 * @param toUnif
	 * @param atom
	 */
	public Partitionner(List<T> list1, List<T> list2) {
		this();
		Iterator<T> it1 = list1.iterator();
		Iterator<T> it2 = list2.iterator();

		while (it1.hasNext()) {
			T e1 = it1.next();
			if (it2.hasNext()) {
				this.add(e1, it2.next());
			}
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	// METHODS
	// //////////////////////////////////////////////////////////////////////////

	public T getRepresentant(T e) {
		for (List<T> clazz : this.partition) {
			if (clazz.contains(e)) {
				return clazz.get(0);
			}
		}
		return e;
	}

	public List<T> getClass(T e) {
		for (List<T> clazz : this) {
			if (clazz.contains(e)) {
				return clazz;
			}
		}
		return null;
	}

	/**
	 * Adds an the specified element in a new partition
	 * @param t
	 */
    public void add(T e) {
        List<T> l = new ArrayList<>();
        l.add(e);
        this.addClass(l);
    }

	/**
	 * Add the couple to the partition
	 */
	public void add(T e, T im) {
		if (!e.equals(im)) {
			List<T> tset = null;
			List<T> imset = null;
			// we look for the equivalence set of e and im if exists
			Iterator<List<T>> ip = partition.iterator();
			while ((tset == null || imset == null) && ip.hasNext()) {
				List<T> s = ip.next();
				Iterator<T> is = s.iterator();
				while ((tset == null || imset == null) && is.hasNext()) {
					T o = is.next();
					if (o.equals(e))
						tset = s;
					if (o.equals(im))
						imset = s;
				}
			}
			// im and e have not equivalence set so we create a new one for its
			if (tset == null && imset == null) {
				List<T> s = new ArrayList<>();
				s.add(e);
				s.add(im);
				partition.add(s);
			}
			// im has not an equivalence set but e has one so we add im in t's
			// equivalence set
			else if (imset == null) {
				tset.add(im);
			}
			// e has not an equivalence set but im has one so we add e in im's
			// equivalence set
			else if (tset == null) {
				imset.add(e);
			}
			// e and im have different equivalence sets so we append the two
			// equivalence set
			else if (tset != imset) {
				tset.addAll(imset);
				partition.remove(imset);
			}
		}
	}

	public void removeClass(List<T> toRemove) {
		partition.remove(toRemove);
	}

	/**
	 * @param toAdd
	 *            (const)
	 */
	public void addClass(List<T> toAdd) {
		Iterator<List<T>> i = partition.iterator();
		List<T> fusion = null;
		while (i.hasNext()) {
			List<T> cl = i.next();
			Iterator<T> it = cl.iterator();
			boolean contain = false;
			while (!contain && it.hasNext()) {
				T e = it.next();
				if (toAdd.contains(e)) {
					contain = true;
					if (fusion == null) {
						fusion = cl;
						cl.addAll(toAdd);
					} else {
						fusion.addAll(cl);
						i.remove();
					}
				}
			}
		}
		if (fusion == null)
			partition.add(new ArrayList<T>(toAdd));
	}

	/**
	 * Return the join of this and the given partition p the join of two
	 * partition is obtained by making the union of their non-disjoint classes
	 * until stability ex: the join of {{a,b,c},{d,e}} and {{e,g},{k,l}} is
	 * {{a,b,c},{d,e,g},{k,l}}
	 */
	public Partitionner<T> join(Partitionner<T> p) {
		Partitionner<T> res = new Partitionner<T>();
		for (List<T> cl : this.partition) {
			res.partition.add(new ArrayList<T>(cl));
		}
		for (List<T> cl : p.partition)
			res.addClass(cl);
		return res;
	}

	@Override
	public Iterator<List<T>> iterator() {
		return partition.iterator();
	}

	// //////////////////////////////////////////////////////////////////////////
	// OBJECT METHODS
	// //////////////////////////////////////////////////////////////////////////

	@Override
	public String toString() {
		return partition.toString();
	}

	@Override
	public int hashCode() {
		int hashCode = 0;
		for (List<T> list : this) {
			hashCode = 17 * hashCode;
			for (T e : list) {
				hashCode = 31 * hashCode + e.hashCode();
			}
		}
		return hashCode * 31 + this.partition.size();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || !(obj instanceof Partitionner)) {
			return false;
		}
		return this.equals((Partitionner<T>) obj);
	}

	public boolean equals(Partitionner<T> other) { // NOPMD
		for (List<T> list : this) {
			for(T e1 : list) {
				for(T e2 : list) {
					List<T> l1 = other.getClass(e1);
					List<T> l2 = other.getClass(e2);
					if (l1 != l2 || l1 == null) {
						return false;
					}
				}
			}
		}
		for (List<T> list : other) {
			for (T e1 : list) {
				for (T e2 : list) {
					List<T> l1 = this.getClass(e1);
					List<T> l2 = this.getClass(e2);
					if (l1 != l2 || l1 == null) {
						return false;
					}
				}
			}
		}
		return true;
	}

}
