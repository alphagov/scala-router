package uk.gov.gds.router.management

import com.google.inject.Singleton
import com.gu.management._
import request.RequestLoggingFilter

@Singleton
class RouterRequestLoggingFilter extends RequestLoggingFilter(metric = Requests, shouldLogParametersOnNonGetRequests = true)

object Requests extends TimingMetric("requests")
