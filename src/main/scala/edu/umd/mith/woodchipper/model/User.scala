package edu.umd.mith.woodchipper.model

import net.liftweb.mapper._
import net.liftweb.util._
import net.liftweb.common._

object User extends User with MetaMegaProtoUser[User] {
  override def dbTableName = "users"
  override def screenWrap = Full(
    <lift:surround with="default" at="content">
      <lift:bind />
    </lift:surround>
  )

  override def fieldOrder = List(
    id, firstName, lastName, email, locale, timezone, password
  )

  override def skipEmailValidation = true
}

class User extends MegaProtoUser[User] {
  def getSingleton = User
}

