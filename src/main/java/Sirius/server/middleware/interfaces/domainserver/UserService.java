package Sirius.server.middleware.interfaces.domainserver;

import java.rmi.*;
import Sirius.server.newuser.*;
import java.util.*;

/***/

public interface UserService extends Remote
{
	// change password
	boolean changePassword(User user, String oldPassword, String newPassword) throws RemoteException, UserException;

	boolean validateUser(User user, String password) throws RemoteException;

	

}