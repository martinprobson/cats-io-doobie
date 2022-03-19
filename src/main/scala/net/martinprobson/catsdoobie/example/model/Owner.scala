package net.martinprobson.catsdoobie.example.model

import net.martinprobson.catsdoobie.example.model.Owner.OWNER_ID

case class Owner(id: OWNER_ID = Owner.UNASSIGNED_OWNER_ID, name: String)

object Owner {

  def apply(id: OWNER_ID, name: String): Owner = new Owner(id, name)
  def apply(name: String): Owner = new Owner(UNASSIGNED_OWNER_ID, name)

  type OWNER_ID = Long
  val UNASSIGNED_OWNER_ID = 0L
}
