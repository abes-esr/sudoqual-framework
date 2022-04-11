/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.abes.sudoqual.util.legacy.sudoqual1.exception;

/**
 *
 * @author clement
 */
@Deprecated
public class InconsistentPartitionException extends Exception {
    
	private static final long serialVersionUID = -7196166243455446425L;

	public InconsistentPartitionException() {
        super("Inconsistent Partition");
    }
    
    public InconsistentPartitionException(Throwable t) {
        super("Inconsistent Partition", t);
    }
    
    public InconsistentPartitionException(String msg) {
        super(msg);
    }    
    
    public InconsistentPartitionException(String msg, Throwable t) {
        super(msg, t);
    }
}
