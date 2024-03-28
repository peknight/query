package com.peknight.query.parser

case class Company(name: String, departments: List[Department]) derives CanEqual
