package org.bitlap.skywalking.apm.plugin.ziogrpc.common

import org.apache.skywalking.apm.network.trace.component.*

object Constants:

  /** Operation name for client has cancelled the call.
   */
  final val REQUEST_ON_CANCEL_OPERATION_NAME: String = "/Request/onCancel"

  /** Operation name for request message received on server or sent on client. <p> Spans of this operations just be
   *  create with request stream calls.
   */
  final val REQUEST_ON_MESSAGE_OPERATION_NAME: String = "/Request/onMessage"

  /** Operation name for request completed all message sending. <p> However, the call may still be cancelled.
   */
  final val REQUEST_ON_HALF_CLOSE_OPERATION_NAME: String = "/Request/onHalfClose"

  /** Operation name for client has completed request sending, there are no more incoming request. <p> It should happen
   *  with half close state usually.
   */
  final val REQUEST_ON_COMPLETE_OPERATION_NAME: String = "/Request/onComplete"

  /** Operation name for call closed with status and trailers. <p> Exceptions will be logs here.
   */
  final val RESPONSE_ON_CLOSE_OPERATION_NAME: String = "/Response/onClose"

  /** Operation name for response message received on client or sent on server. <p> Spans of this operations just be
   *  create with response stream calls.
   */
  final val RESPONSE_ON_MESSAGE_OPERATION_NAME: String = "/Response/onMessage"

  final val SERVER: String              = "/server"
  final val CLIENT: String              = "/client"
  final val ZIO_GRPC: OfficialComponent = ComponentsDefine.GRPC

end Constants
