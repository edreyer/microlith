package io.liquidsoftware.base.user.adapter.out.persistence

import com.fasterxml.jackson.annotation.JsonInclude
import io.liquidsoftware.base.user.UserNamespace
import io.liquidsoftware.base.user.application.port.`in`.UserDisabledEvent
import io.liquidsoftware.base.user.application.port.`in`.UserEnabledEvent
import io.liquidsoftware.base.user.application.port.`in`.UserEvent
import io.liquidsoftware.base.user.domain.Role
import io.liquidsoftware.common.persistence.BaseEntity
import io.liquidsoftware.common.security.acl.Acl
import io.liquidsoftware.common.security.acl.AclRole
import org.hibernate.annotations.Filter
import org.hibernate.annotations.FilterDef
import org.hibernate.annotations.Where
import javax.persistence.CollectionTable
import javax.persistence.Column
import javax.persistence.ElementCollection
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.FetchType
import javax.persistence.JoinColumn
import javax.persistence.Table

@Entity
@Table(name = "users")
@Where(clause = "deleted_at is null")
@FilterDef(name = "deletedProductFilter")
@Filter(name = "deletedProductFilter", condition = "deleted_at is not null")
internal class UserEntity(

  userId: String,

  var msisdn: String,

  var email: String,

  @Column(name = "encrypted_password")
  var password: String,

  @ElementCollection(fetch = FetchType.EAGER)
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  @Enumerated(EnumType.STRING)
  @CollectionTable(name = "user_roles", joinColumns = [JoinColumn(name = "user_id")])
  @Column(name = "role")
  var roles: MutableList<Role>,

  var active: Boolean = true

) : BaseEntity(userId, UserNamespace.NAMESPACE) {

  fun acl() = Acl.of(id, id, AclRole.MANAGER)

  suspend fun handle(event: UserEvent): UserEntity {
    return when(event) {
      is UserEnabledEvent -> handle(event)
      is UserDisabledEvent -> handle(event)
      else -> this
    }
  }

  private suspend fun handle(event: UserEnabledEvent): UserEntity {
    this.active = true
    return this
  }

  private suspend fun handle(event: UserDisabledEvent): UserEntity {
    this.active = false
    return this
  }

}
