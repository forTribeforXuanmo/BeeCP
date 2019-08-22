/*
 * Copyright Chris2018998
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jmin.bee.pool;

/**
 * pool objects state
 *
 * @author Chris.Liao
 * @version 1.0
 */

class PoolObjectsState {
	
	//POOL STATE
	public static final int POOL_UNINIT         = 0;
	public static final int POOL_NORMAL         = 1;
	public static final int POOL_CLOSED         = 2;
 
	//POOLED CONNECTION STATE
	public static final int CONNECTION_IDLE      = 0;
	public static final int CONNECTION_USING     = 1;
	public static final int CONNECTION_CLOSED    = 2;
	
	//WORK THREAD STATE
	public static final int THREAD_NORMAL         = 0;
	public static final int THREAD_WAITING        = 1;
	public static final int THREAD_WORKING        = 2;
	public static final int THREAD_DEAD           = 3;  
	
	//BORROWER THREAD ACTIVE STATE
	static class BorrowStatus{}
	public static final Object BORROWER_NORMAL    = new BorrowStatus();
	public static final Object BORROWER_WAITING   = new BorrowStatus();
	public static final Object BORROWER_TIMEOUT   = new BorrowStatus();
	
}
