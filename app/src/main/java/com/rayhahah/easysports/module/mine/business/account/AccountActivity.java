package com.rayhahah.easysports.module.mine.business.account;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.text.InputType;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.rayhahah.dialoglib.CustomDialog;
import com.rayhahah.dialoglib.DialogInterface;
import com.rayhahah.dialoglib.NormalSelectionDialog;
import com.rayhahah.easysports.R;
import com.rayhahah.easysports.app.MyApp;
import com.rayhahah.easysports.bean.db.LocalUser;
import com.rayhahah.easysports.common.BaseActivity;
import com.rayhahah.easysports.common.C;
import com.rayhahah.easysports.databinding.ActivityAccountBinding;
import com.rayhahah.easysports.databinding.DialogEdittextSettingBinding;
import com.rayhahah.easysports.module.mine.bean.MineListBean;
import com.rayhahah.easysports.module.mine.domain.AccountListAdapter;
import com.rayhahah.easysports.utils.glide.GlideCircleTransform;
import com.rayhahah.easysports.view.TitleItemDecoration;
import com.rayhahah.rbase.bean.MsgEvent;
import com.rayhahah.rbase.utils.base.DialogUtil;
import com.rayhahah.rbase.utils.base.FileUtils;
import com.rayhahah.rbase.utils.base.StringUtils;
import com.rayhahah.rbase.utils.base.ToastUtils;
import com.rayhahah.rbase.utils.useful.GlideUtil;
import com.rayhahah.rbase.utils.useful.SPManager;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AccountActivity extends BaseActivity<AccountPresenter, ActivityAccountBinding>
        implements AccountContract.IAccountView, View.OnClickListener, BaseQuickAdapter.OnItemChildClickListener {

    private AccountListAdapter mAdapter;
    private LocalUser mLocalUser;
    private CustomDialog mCustomDialog;

    public static void start(Context context, Activity preActivity) {
        toNextActivity(context, AccountActivity.class, preActivity);
    }

    @Override
    protected AccountPresenter getPresenter() {
        return new AccountPresenter(this);
    }

    @Override
    protected int getLayoutID() {
        return R.layout.activity_account;
    }

    @Override
    protected void initEventAndData(Bundle savedInstanceState) {
        initToolbar();
        initView();
        initRv();
    }

    @Override
    protected int setFragmentContainerResId() {
        return 0;
    }

    @Override
    public void showViewLoading() {

    }

    @Override
    public void showViewError(Throwable t) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_toolbar_back:
                EventBus.getDefault().post(new MsgEvent(C.EventAction.UPDATE_CURRENT_USER, null));
                finish();
                break;
            case R.id.btn_account_logout:
                SPManager.get().putString(C.SP.CURRENT_USER, C.NULL);
                SPManager.get().putString(C.SP.IS_LOGIN, C.FALSE);
                MyApp.setCurrentUser(null);
                EventBus.getDefault().post(new MsgEvent(C.EventAction.UPDATE_CURRENT_USER, null));
                finish();
                break;
            case R.id.iv_account_cover:
                NormalSelectionDialog dialog = createSelectDialog();
                ArrayList<String> datas = new ArrayList<>();
                datas.add(getResources().getString(R.string.take_photo));
                datas.add(getResources().getString(R.string.select_photo));
                dialog.setDatas(datas).show();
                break;
        }
    }

    @Override
    public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
        List<MineListBean> data = adapter.getData();
        MineListBean bean = data.get(position);
        switch (bean.getId()) {
            //设置昵称
            case C.ACCOUNT.ID_SCREENNAME:
                dismiss();
                mCustomDialog = createDialog(createScreenDialog().getRoot());
                mCustomDialog.show();
                break;
            //重置密码
            case C.ACCOUNT.ID_RESET_PASSWORD:
                dismiss();
                mCustomDialog = createDialog(createPasswordDialog().getRoot());
                mCustomDialog.show();
                break;
            //虎扑账号设置
            case C.ACCOUNT.ID_HUPU:
                dismiss();
                mCustomDialog = createDialog(createHupuDialog().getRoot());
                mCustomDialog.show();
                break;
            //电话号码设置
            case C.ACCOUNT.ID_TEL:
                // TODO: 2017/7/25 短信验证

                break;
        }
    }

    @Override
    public void getCurrentUserSuccess(LocalUser localUser) {
        mLocalUser = localUser;
        mBinding.tvAccountScreenname.setText(mLocalUser.getScreen_name());
        if (C.NULL.equals(mLocalUser.getCover())) {
            GlideUtil.loadWithTransform(mContext, R.mipmap.ic_logo, mBinding.ivAccountCover
                    , new GlideCircleTransform(mContext));
        } else {
            GlideUtil.loadWithTransform(mContext, mLocalUser.getCover(), mBinding.ivAccountCover
                    , new GlideCircleTransform(mContext));
        }
    }

    @Override
    public void updateUserSuccess() {
        getCurrentUserSuccess(MyApp.getCurrentUser());
        DialogUtil.dismissDialog();
        ToastUtils.showShort("设置信息成功");
    }

    @Override
    public void updateUserFailed() {
        DialogUtil.dismissDialog();
        ToastUtils.showShort("设置信息失败");
    }

    @Override
    public void uploadCoverSuccess(String url) {
        DialogUtil.dismissDialog();
        DialogUtil.showLoadingDialog(mContext, "正在设置信息");
        LocalUser localUser = mLocalUser;
        localUser.setCover(url);
        mPresenter.updateUser(localUser);
    }

    @Override
    public void uploadCoverFailed(int code, String msg) {
        DialogUtil.dismissDialog();
        ToastUtils.showShort("上传图片失败");
    }


    private void initToolbar() {
        mBinding.toolbar.ivToolbarBack.setVisibility(View.VISIBLE);
        mBinding.toolbar.ivToolbarBack.setOnClickListener(this);
        mBinding.toolbar.tvToolbarTitle.setText(getResources().getString(R.string.account_manage));
    }


    private void initView() {
        getCurrentUserSuccess(MyApp.getCurrentUser());
        mBinding.btnAccountLogout.setOnClickListener(this);
        mBinding.ivAccountCover.setOnClickListener(this);
    }

    private void initRv() {
        mBinding.rvAccountList.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        final List<MineListBean> mData = mPresenter.getListData(mContext);
        mAdapter = new AccountListAdapter(mData);
        mAdapter.setOnItemChildClickListener(this);
        TitleItemDecoration decor = new TitleItemDecoration(mContext
                , mThemeColorMap.get(C.ATTRS.COLOR_BG)
                , mThemeColorMap.get(C.ATTRS.COLOR_PRIMARY_DARK)
                , mThemeColorMap.get(C.ATTRS.COLOR_BG)
                , TitleItemDecoration.GRAVITY_MIDDLE
                , new TitleItemDecoration.DecorationCallback() {
            @Override
            public String getGroupId(int position) {
                if (position < mData.size()
                        && StringUtils.isNotEmpty(mData.get(position).getSectionData())) {
                    return mData.get(position).getSectionData();
                }
                return null;
            }

        }, new TitleItemDecoration.TitleTextCallback() {
            @Override
            public String getGroupFirstLine(int position) {
                if (position < mData.size()
                        && StringUtils.isNotEmpty(mData.get(position).getSectionData())) {
                    return mData.get(position).getSectionData();
                }
                return "";
            }

            @Override
            public String getActiveGroup() {
                return getResources().getString(R.string.account_setting);
            }
        });
        mBinding.rvAccountList.addItemDecoration(decor);
        mBinding.rvAccountList.setAdapter(mAdapter);
    }

    /**
     * 判断输入是否合法
     *
     * @param settingBinding
     * @return
     */
    private boolean checkInputLegal(DialogEdittextSettingBinding settingBinding) {
        if (!StringUtils.isLegalUsername(settingBinding.etEditTwo.getText().toString())) {
            ToastUtils.showShort(getResources().getString(R.string.password) + "必须是字母和数字的结合");
            return false;
        }
        if (!StringUtils.isNotEmpty(settingBinding.etEditOne.getText().toString())) {
            ToastUtils.showShort("原" + getResources().getString(R.string.password) + getResources().getString(R.string.not_null));
            return false;
        }
        if (!StringUtils.isNotEmpty(settingBinding.etEditTwo.getText().toString())) {
            ToastUtils.showShort("新" + getResources().getString(R.string.password) + getResources().getString(R.string.not_null));
            return false;
        }
        if (!StringUtils.isNotEmpty(settingBinding.etEditThree.getText().toString())) {
            ToastUtils.showShort("确认" + getResources().getString(R.string.password) + getResources().getString(R.string.not_null));
            return false;
        }

        if (!settingBinding.etEditTwo.getText().toString().equals(settingBinding.etEditThree.getText().toString())) {
            ToastUtils.showShort("确认密码需保持一致");
            return false;
        }

        if (!settingBinding.etEditOne.getText().toString().equals(mLocalUser.getPassword())) {
            ToastUtils.showShort("原密码错误");
            return false;
        }
        return true;
    }

    /**
     * 重置密码弹窗
     */
    @NonNull
    private DialogEdittextSettingBinding createPasswordDialog() {
        final DialogEdittextSettingBinding settingBinding = (DialogEdittextSettingBinding) DataBindingUtil.inflate(getLayoutInflater()
                , R.layout.dialog_edittext_setting, null, false);
        settingBinding.tvDescOne.setText("原" + getResources().getString(R.string.password));
        settingBinding.etEditOne.setHint("请输入现在的" + getResources().getString(R.string.password));

        settingBinding.tvDescTwo.setText("新" + getResources().getString(R.string.password));
        settingBinding.etEditTwo.setHint("请输入新的" + getResources().getString(R.string.password));

        settingBinding.tvDescThree.setText("确认" + getResources().getString(R.string.password));
        settingBinding.etEditThree.setHint("请重新确认" + getResources().getString(R.string.password));
        settingBinding.etEditOne.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        settingBinding.etEditTwo.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        settingBinding.etEditThree.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);

        settingBinding.btnEditConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkInputLegal(settingBinding)) {
                    return;
                }
                DialogUtil.showLoadingDialog(mContext, "正在重置密码");
                LocalUser localUser = mLocalUser;
                localUser.setPassword(settingBinding.etEditTwo.getText().toString());
                mPresenter.updateUser(localUser);
                dismiss();
            }
        });
        settingBinding.btnEditCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        return settingBinding;
    }


    /**
     * 修改昵称弹窗
     */
    @NonNull
    private DialogEdittextSettingBinding createScreenDialog() {
        final DialogEdittextSettingBinding settingBinding = (DialogEdittextSettingBinding) DataBindingUtil.inflate(getLayoutInflater()
                , R.layout.dialog_edittext_setting, null, false);
        settingBinding.tvDescOne.setVisibility(View.VISIBLE);
        settingBinding.tvDescOne.setText("新" + getResources().getString(R.string.screenname));
        settingBinding.etEditOne.setVisibility(View.VISIBLE);
        settingBinding.etEditOne.setHint("请输入新的" + getResources().getString(R.string.screenname));
        settingBinding.etEditTwo.setVisibility(View.GONE);
        settingBinding.etEditThree.setVisibility(View.GONE);
        settingBinding.tvDescTwo.setVisibility(View.GONE);
        settingBinding.tvDescThree.setVisibility(View.GONE);

        settingBinding.btnEditConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LocalUser localUser = mLocalUser;
                String screen_name = settingBinding.etEditOne.getText().toString();
                if (StringUtils.isNotEmpty(screen_name)) {
                    localUser.setScreen_name(screen_name);
                    DialogUtil.showLoadingDialog(mContext, "正在更新");
                    mPresenter.updateUser(localUser);
                    dismiss();
                } else {
                    ToastUtils.showShort(getResources().getString(R.string.screenname) + getResources().getString(R.string.not_null));
                }
            }
        });
        settingBinding.btnEditCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        return settingBinding;
    }

    private DialogEdittextSettingBinding createHupuDialog() {
        final DialogEdittextSettingBinding settingBinding = (DialogEdittextSettingBinding) DataBindingUtil.inflate(getLayoutInflater()
                , R.layout.dialog_edittext_setting, null, false);
        settingBinding.tvDescOne.setText(getResources().getString(R.string.hupu) + getResources().getString(R.string.username));
        settingBinding.etEditOne.setHint("请输入虎扑的" + getResources().getString(R.string.username));

        settingBinding.tvDescTwo.setText(getResources().getString(R.string.hupu) + getResources().getString(R.string.password));
        settingBinding.etEditTwo.setHint("请输入虎扑的" + getResources().getString(R.string.password));

        settingBinding.etEditTwo.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);

        settingBinding.etEditThree.setVisibility(View.GONE);
        settingBinding.tvDescThree.setVisibility(View.GONE);

        settingBinding.btnEditConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (StringUtils.isNotEmpty(settingBinding.etEditOne.getText().toString())
                        && StringUtils.isNotEmpty(settingBinding.etEditTwo.getText().toString())) {
                    DialogUtil.showLoadingDialog(mContext, "正在绑定虎扑");
                    LocalUser localUser = mLocalUser;
                    localUser.setHupu_user_name(settingBinding.etEditOne.getText().toString());
                    localUser.setHupu_password(settingBinding.etEditTwo.getText().toString());
                    mPresenter.updateUser(localUser);
                    dismiss();
                } else {
                    ToastUtils.showShort(getResources().getString(R.string.message) + getResources().getString(R.string.not_null));
                    return;
                }
            }
        });
        settingBinding.btnEditCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        return settingBinding;
    }


    private void dismiss() {
        if (mCustomDialog != null && mCustomDialog.isShowing()) {
            mCustomDialog.dismiss();
            mCustomDialog = null;
        }
    }

    private CustomDialog createDialog(View root) {
        CustomDialog customDialog = new CustomDialog.Builder(mContext)
                .setView(root)
                .setTouchOutside(true)
                .build();
        return customDialog;
    }

    /**
     * 初始化底部选择Dialog
     *
     * @return
     */
    private NormalSelectionDialog createSelectDialog() {
        return new NormalSelectionDialog.Builder(mContext)
                .setlTitleVisible(false)
                .setItemHeight(50)  //设置item的高度
                .setItemWidth(0.9f)  //屏幕宽度*0.9
                .setItemTextColor(mThemeColorMap.get(C.ATTRS.COLOR_PRIMARY))  //设置item字体颜色
                .setItemTextSize(18)  //设置item字体大小
                .setCancleButtonText(getResources().getString(R.string.cancel))  //设置最底部“取消”按钮文本
                .setTopBgResResources(R.drawable.selector_actiondialog_top_color_bg)
                .setMiddleBgResResources(R.drawable.selector_actiondialog_middle_color_bg)
                .setCancelBgResResources(R.drawable.selector_bg_click_corner)
                .setBottomBgResResources(R.drawable.selector_actiondialog_bottom_color_bg)
                .setSingleBgResResources(R.drawable.selector_actiondialog_single_color_bg)
                .setOnItemListener(new DialogInterface.OnItemClickListener<NormalSelectionDialog>() {
                    @Override
                    public void onItemClick(NormalSelectionDialog dialog1, View button, int position) {
                        switch (position) {
                            case 0:
                                mPresenter.takePhoto(mContext);
                                break;
                            case 1:
                                mPresenter.choosePhoto(mContext);
                                break;
                        }
                        dialog1.dismiss();
                    }
                })
                .setCanceledOnTouchOutside(true)
                .build();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String path = "";
        switch (requestCode) {
            case C.ACCOUNT.CODE_TAKE_PHOTO:
                path = FileUtils.getPathFromUri(mContext, mPresenter.getUri());
                File file = new File(path);
                if (file.length() > 0) {
                    DialogUtil.showProgressDialog(mContext, "正在上传图片");
                    mPresenter.uploadCover(path);
                }
                break;
            case C.ACCOUNT.CODE_CHOOSE_PHOTO:
                if (data != null) {
                    path = FileUtils.getPathFromUri(mContext, data.getData());
                    DialogUtil.showProgressDialog(mContext, "正在上传图片");
                    mPresenter.uploadCover(path);
                }
                break;
        }
    }
}
