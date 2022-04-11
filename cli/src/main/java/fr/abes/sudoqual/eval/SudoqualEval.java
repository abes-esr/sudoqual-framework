package fr.abes.sudoqual.eval;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import fr.abes.sudoqual.util.exception.SudoqualInternalException;
import fr.abes.sudoqual.util.legacy.sudoqual1.IPartition;
import fr.abes.sudoqual.util.legacy.sudoqual1.Partition;
import fr.abes.sudoqual.util.legacy.sudoqual1.exception.InconsistentPartitionException;

public final class SudoqualEval {

	private SudoqualEval() {
	}
	
	public static EvalResult evalResults(JSONArray sourcesToCheck, JSONArray actualLinks, JSONArray expectedLinks) {
		try {
			IPartition actualPart = partitionFromLinksArray(actualLinks);
			IPartition expectedPart = partitionFromLinksArray(expectedLinks);
			sourcesToCheck.forEach(s -> {actualPart.addContextual((String)s); expectedPart.addContextual((String)s);});
			return evalResults(actualPart, expectedPart);
		} catch (InconsistentPartitionException e) {
			throw new RuntimeException(new SudoqualInternalException("An exception occured while constructing partitions.", e));
		}
	}
	
	private static IPartition partitionFromLinksArray(JSONArray array) throws InconsistentPartitionException {
		Partition partition = new Partition();
		for(Object o : array) {
			if(!(o instanceof JSONObject)) {
				throw new IllegalArgumentException("The array must only contains JSONObject.");
			}
			JSONObject link = (JSONObject)o;
			String source = link.getString("source");
			String target = link.getString("target");
			
			switch(link.getString("type")) {
			case "sameAs":
				partition.setSameAsLink(source, target);
				break;
			case "suggestedSameAs":
				partition.addSuggestedLink(source, target);
				break;
			case "diffFrom":
				partition.addDifferentFromLink(source, target);
				break;
			}
		}
		return partition;
	}

	private static EvalResult evalResults(IPartition partition, IPartition partitionValidation) {
		EvalResult res = new EvalResult();

		Set<String> listRCWithValidation = partitionValidation.getContextuals();

		for (String rc : partition.getContextuals()) {
			if (!listRCWithValidation.contains(rc)) {
				System.err.println("The following RC have no expected diagnostic: " + rc);
				continue;
			}
			// cas 1/?
			if (partition.getAuthoritySameAs(rc) != null) {
				String sameAs = partition.getAuthoritySameAs(rc);
				// cas 1 - Le diagnostic calculé et le diagnostic attendu contiennent un lien
				// sûr
				if (sameAs.equals(partitionValidation.getAuthoritySameAs(rc))) {
					System.err.print("\n          good (case 1): " + rc);
					res.incrGood();
				}
				// cas 2 - Seul le diagnostic calculé contient un lien sûr
				else {
					if (partitionValidation.getAuthorityDifferentFrom(rc).contains(sameAs)) {
						System.err.print("\n           bad (case 2): " + rc);
						res.incrBad();
					} else {
						System.err.print("\nunsatisfactory (case 2): " + rc);
						res.incrUnsatisfactory();
					}
				}
			}
			// cas 3 - Seul le diagnostic attendu contient un lien sûr
			else if (partitionValidation.getAuthoritySameAs(rc) != null) {
				String sameAsAtt = partitionValidation.getAuthoritySameAs(rc);
				Set<String> dfo = partition.getAuthorityDifferentFrom(rc);
				Set<String> slo = partition.getAuthoritySuggestedLinksOf(rc);
				if (slo.contains(sameAsAtt)) {
					System.err.print("\n       careful (case 3): " + rc);
					res.incrCareful();
				} else if (dfo.contains(sameAsAtt)) {
					System.err.print("\n           bad (case 3): " + rc);
					res.incrBad();
				} else {
					System.err.print("\nunsatisfactory (case 3): " + rc);
					res.incrUnsatisfactory();
				}
			}
			// cas 4 - Ni le diagnostic calculé, ni le diagnostic attendu ne contiennent de
			// lien sûr
			else {
				Set<String> slo = partition.getAuthoritySuggestedLinksOf(rc);
				Set<String> sla = partitionValidation.getAuthoritySuggestedLinksOf(rc);
				Set<String> dfo = partition.getAuthorityDifferentFrom(rc);
				Set<String> dfa = partitionValidation.getAuthorityDifferentFrom(rc);

				if (slo.equals(sla) && dfo.equals(dfa)) {
					if(!slo.isEmpty() || !dfo.isEmpty()) {
    					System.err.print("\n          good (case 2): " + rc);
    					res.incrGood();
					} else {
						System.err.print("\n  warning : " + rc + " no sameAs, suggested or diffFrom expected.");
						res.incrWarn();
					}
				} else if (containsOneOf(slo, dfa) || containsOneOf(dfo, sla)) {
					System.err.print("\nunsatisfactory (case bad): " + rc);
					res.incrUnsatisfactory();
				} else if (sla.containsAll(slo) && dfa.containsAll(dfo)) {
					System.err.print("\n       careful (case 4): " + rc);
					res.incrCareful();
				} else {
					System.err.print("\nunsatisfactory (case 4): " + rc);
					res.incrUnsatisfactory();
				}
			}
		}

		return res;
	}
	
	private static boolean containsOneOf(Collection<?> c, Collection<?> from) {
		for(Object e : from) {
			if(c.contains(e)) {
				return true;
			}
		}
		return false;
	}

}
