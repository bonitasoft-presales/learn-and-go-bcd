package com.company.rest.api

import java.time.Instant
import java.util.logging.Logger

class Tracer {
	
	Logger logger
	def ret
	
	public Tracer(Logger logger, def ret) {
		this.logger=logger
		this.ret=ret
	}
	def trace(def key, def message) {
		logger.info("$key : $message" )
		def now = Instant.now()
		ret.put("$now | $key", message )
	}
}
